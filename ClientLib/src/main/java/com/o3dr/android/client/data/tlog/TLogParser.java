package com.o3dr.android.client.data.tlog;

import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Parser;
import com.o3dr.android.client.data.tlog.callback.TLogIteratorCallback;
import com.o3dr.android.client.data.tlog.callback.TLogIteratorFilter;
import com.o3dr.android.client.data.tlog.callback.TLogParserCallback;
import com.o3dr.android.client.data.tlog.callback.TLogParserFilter;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Parse TLog file into Events
 */
public class TLogParser {
    private static final String LOG_TAG = TLogParser.class.getSimpleName();

    private static final Parser parser = new Parser();
    private static ExecutorService parseTlogExecutor;

    public static class TLogIterator {
        private File file;
        private DataInputStream in = null;
        private ExecutorService iteratorExecutor;
        private final Handler handler;

        /**
         * Constructor to create a TLogIterator with new Handler.
         *
         * @param uri Location of the TLog files
         */
        public TLogIterator(Uri uri) {
            this(uri, new Handler());
        }

        /**
         * Constructor to create a TLogIterator with a specified Handler.
         *
         * @param uri     Location of the TLog files
         * @param handler Handler to post results to
         */
        public TLogIterator(Uri uri, Handler handler) {
            this.handler = handler;
            file = new File(uri.toString());
        }

        /**
         * Opens TLog file to begin iterating.
         *
         * @throws FileNotFoundException
         */
        public void start() throws FileNotFoundException {
            iteratorExecutor = Executors.newSingleThreadExecutor();
            in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
        }

        /**
         * Closes TLog file
         *
         * @throws IOException
         */
        public void finish() throws IOException {
            iteratorExecutor.shutdownNow();
            in.close();
        }

        /**
         * Retrieve next message from TLog file asynchronously.
         * start must be called before this method.
         *
         * @param callback
         */
        public void nextAsync(final TLogIteratorCallback callback) {
            nextAsync(new TLogIteratorFilter() {
                @Override
                public boolean acceptEvent(Event event) {
                    return true;
                }
            }, callback);
        }

        /**
         * Retrieve next message with specified message filter from TLog file asynchronously.
         * start must be called before this method.
         *
         * @param filter
         * @param callback
         */
        public void nextAsync(final TLogIteratorFilter filter, final TLogIteratorCallback callback) {
            iteratorExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Event event;
                        do {
                            event = next(in);
                            if (filter.acceptEvent(event)) {
                                sendResult(callback, event);
                                return;
                            }
                        } while (event != null);
                        sendResult(callback, null);
                    } catch (IOException e) {
                        sendFailed(callback, e);
                    }
                }
            });
        }

        private void sendResult(final TLogIteratorCallback callback, final Event event) {
            if (callback != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResult(event);
                    }
                });
            }
        }

        private void sendFailed(final TLogIteratorCallback callback, final Exception e) {
            if (callback != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFailed(e);
                    }
                });
            }
        }
    }

    /**
     * Returns a list of all events in specified TLog uri
     *
     * @param handler Thread to callback on. Cannot be null.
     * @param uri
     * @param callback
     */
    public static void getAllEventsAsync(final Handler handler, final Uri uri, final TLogParserCallback callback) {
        parseTlogExecutor = Executors.newSingleThreadExecutor();
        parseTlogExecutor.execute(new Runnable() {
            @Override
            public void run() {
                File file = new File(uri.toString());
                DataInputStream in = null;
                LinkedList<Event> eventList = new LinkedList<>();
                try {
                    in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
                    Event event = next(in);
                    while (event != null) {
                        eventList.add(event);
                        event = next(in);
                    }

                    sendResult(handler, callback, eventList);
                } catch (FileNotFoundException e) {
                    sendFailed(handler, callback, e);
                } catch (IOException e) {
                    sendFailed(handler, callback, e);
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            Log.e(LOG_TAG, "Failed to close file " + uri, e);
                        }
                    }
                }
            }
        });
    }

    /**
     * Returns a list of all events in specified TLog uri using the specified filter
     *
     * @param handler Thread to callback on. Cannot be null.
     * @param uri
     * @param filter
     * @param callback
     */
    public static void getAllEventsAsync(final Handler handler, final Uri uri, final TLogParserFilter filter, final TLogParserCallback callback) {
        parseTlogExecutor = Executors.newSingleThreadExecutor();
        parseTlogExecutor.execute(new Runnable() {
            @Override
            public void run() {
                File file = new File(uri.toString());
                DataInputStream in = null;
                LinkedList<Event> eventList = new LinkedList<>();
                try {
                    in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
                    Event event = next(in);
                    while (event != null && filter.continueIterating()) {
                        eventList.add(event);
                        event = next(in);
                    }

                    sendResult(handler, callback, eventList);
                } catch (FileNotFoundException e) {
                    sendFailed(handler, callback, e);
                } catch (IOException e) {
                    sendFailed(handler, callback, e);
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            Log.e(LOG_TAG, "Failed to close file " + uri, e);
                        }
                    }
                }
            }
        });
    }

    private static void sendResult(Handler handler, final TLogParserCallback callback, final List<Event> events) {
        if (callback != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onResult(events);
                }
            });
        }
    }

    private static void sendFailed(Handler handler, final TLogParserCallback callback, final Exception e) {
        if (callback != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onFailed(e);
                }
            });
        }
    }

    private static Event next(DataInputStream in) throws IOException {
        try {
            long timestamp = in.readLong() / 1000;
            MAVLinkPacket packet;
            while ((packet = parser.mavlink_parse_char(in.readUnsignedByte())) == null) ;
            return new Event(timestamp, packet.unpack());
        } catch (EOFException e) {
            //File may not be complete so return null
            return null;
        }
    }

    /**
     * Mavlink message event.
     */
    public static class Event {
        private long timestamp;
        private MAVLinkMessage mavLinkMessage;

        public Event(long timestamp, MAVLinkMessage mavLinkMessage) {
            this.timestamp = timestamp;
            this.mavLinkMessage = mavLinkMessage;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public MAVLinkMessage getMavLinkMessage() {
            return mavLinkMessage;
        }
    }
}

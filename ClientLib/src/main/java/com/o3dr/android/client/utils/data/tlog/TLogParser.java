package com.o3dr.android.client.utils.data.tlog;

import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Parser;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Parse TLog file into Events
 */
public class TLogParser {
    private static final String LOG_TAG = TLogParser.class.getSimpleName();

    private static final Parser parser = new Parser();

    //Private constructor to prevent instantiation.
    private TLogParser(){}

    /**
     * Iterator class to iterate and parse the Tlog file.
     */
    public static class TLogIterator {
        private File file;
        private DataInputStream in = null;
        private final Handler handler;

        private static final TLogIteratorFilter DEFAULT_FILTER = new TLogIteratorFilter() {
            @Override
            public boolean acceptEvent(Event event) {
                return true;
            }
        };

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
            in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
        }

        /**
         * Closes TLog file
         *
         * @throws IOException
         */
        public void finish() throws IOException {
            in.close();
        }

        /**
         * Retrieve next message from TLog file asynchronously.
         * {@link #start()} must be called before this method.
         *
         * @param callback {@link TLogIteratorCallback}
         */
        public void nextAsync(final TLogIteratorCallback callback) {
            nextAsync(DEFAULT_FILTER, callback);
        }

        /**
         * Retrieve next message with specified message filter from TLog file asynchronously.
         * {@link #start()} must be called before this method.
         *
         * @param filter {@link TLogIteratorFilter}
         * @param callback {@link TLogIteratorCallback}
         */
        public void nextAsync(final TLogIteratorFilter filter, final TLogIteratorCallback callback) {
            getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Event event = next(in);
                        while (event != null) {
                            if (filter.acceptEvent(event)) {
                                sendResult(callback, event);
                                return;
                            }
                            event = next(in);
                        }

                        sendFailed(callback, new NoSuchElementException());
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

    private static final TLogParserFilter DEFAULT_FILTER = new TLogParserFilter() {
        @Override
        public boolean includeEvent(Event e) {
            return true;
        }

        @Override
        public boolean shouldIterate() {
            return true;
        }
    };

    /**
     * Returns a list of all events in specified TLog uri
     *
     * @param handler {@link Handler} Handler to specify what thread to callback on. This cannot be null.
     * @param uri {@link Uri}
     * @param callback {@link TLogParserCallback}
     */
    public static void getAllEventsAsync(final Handler handler, final Uri uri, final TLogParserCallback callback) {
        getAllEventsAsync(handler, uri, DEFAULT_FILTER, callback);
    }

    /**
     * Returns a list of all events in specified TLog uri using the specified filter
     *
     * @param handler {@link Handler} Handler to specify what thread to callback on. This cannot be null.
     * @param uri {@link Uri}
     * @param filter {@link TLogParserFilter}
     * @param callback {@link TLogParserCallback}
     */
    public static void getAllEventsAsync(final Handler handler, final Uri uri, final TLogParserFilter filter, final TLogParserCallback callback) {
        getInstance().execute(new Runnable() {
            @Override
            public void run() {
                File file = new File(uri.getPath());
                DataInputStream in = null;
                try {
                    in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
                    ArrayList<Event> eventList = new ArrayList<>();
                    Event event = next(in);
                    while (event != null && filter.shouldIterate()) {
                        if (filter.includeEvent(event)) {
                            eventList.add(event);
                        }
                        event = next(in);
                    }

                    if (eventList.isEmpty()) {
                        sendFailed(handler, callback, new NoSuchElementException());
                    } else {
                        sendResult(handler, callback, eventList);
                    }
                } catch (Exception e) {
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
            while ((packet = parser.mavlink_parse_char(in.readUnsignedByte())) == null);
            MAVLinkMessage message = packet.unpack();
            if (message == null) {
                return null;
            }
            return new Event(timestamp, message);
        } catch (EOFException e) {
            //File may not be complete so return null
            return null;
        }
    }

    private static ExecutorService getInstance() {
        return InitializeExecutorService.executorService;
    }

    private static class InitializeExecutorService {
        private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Mavlink message event.
     */
    public static class Event {
        private long timestamp;
        private MAVLinkMessage mavLinkMessage;

        private Event(long timestamp, MAVLinkMessage mavLinkMessage) {
            this.timestamp = timestamp;
            this.mavLinkMessage = mavLinkMessage;
        }

        /**
         * Returns time of mavlink message in ms
         *
         * @return
         */
        public long getTimestamp() {
            return timestamp;
        }

        /**
         * Returns content of mavlink message.
         *
         * @return {@link MAVLinkMessage}
         */
        public MAVLinkMessage getMavLinkMessage() {
            return mavLinkMessage;
        }
    }
}

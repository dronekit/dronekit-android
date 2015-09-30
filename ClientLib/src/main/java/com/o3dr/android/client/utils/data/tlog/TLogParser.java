package com.o3dr.android.client.utils.data.tlog;

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
import java.net.URI;
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
        public TLogIterator(URI uri) {
            this(uri, new Handler());
        }

        /**
         * Constructor to create a TLogIterator with a specified Handler.
         *
         * @param uri     Location of the TLog files
         * @param handler Handler to post results to
         */
        public TLogIterator(URI uri, Handler handler) {
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
            iteratorExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        sendResult(callback, next(in));
                    } catch (EOFException e) {
                        sendResult(callback, null);
                    } catch (IOException e) {
                        sendFailed(callback, e);
                    }
                }
            });
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
                        Event event = null;
                        while (in.available() > 0) {
                            Event currEvent = next(in);
                            if (filter.acceptEvent(currEvent)) {
                                event = currEvent;
                                break;
                            }
                        }
                        sendResult(callback, event);
                    } catch (EOFException e) {
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
     * @param uri
     * @param callback
     */
    public static void getAllEventsAsync(final URI uri, final TLogParserCallback callback) {
        parseTlogExecutor = Executors.newSingleThreadExecutor();
        parseTlogExecutor.execute(new Runnable() {
            @Override
            public void run() {
                File file = new File(uri.toString());
                DataInputStream in = null;
                LinkedList<Event> eventList = new LinkedList<>();
                try {
                    in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
                    while (in.available() > 0) {
                        eventList.add(next(in));
                    }
                    callback.onResult(eventList);
                } catch (EOFException e) {
                    callback.onResult(eventList);
                } catch (FileNotFoundException e) {
                    callback.onFailed(e);
                } catch (IOException e) {
                    callback.onFailed(e);
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
     * @param uri
     * @param filter
     * @param callback
     */
    public static void getAllEventsAsync(final URI uri, final TLogParserFilter filter, final TLogParserCallback callback) {
        parseTlogExecutor = Executors.newSingleThreadExecutor();
        parseTlogExecutor.execute(new Runnable() {
            @Override
            public void run() {
                File file = new File(uri.toString());
                DataInputStream in = null;
                LinkedList<Event> eventList = new LinkedList<>();
                try {
                    in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
                    while (in.available() > 0 && filter.continueIterating()) {
                        Event event = next(in);
                        if (filter.addEventToList(event)) {
                            eventList.add(event);
                        }
                    }
                    sendResult(callback, eventList);
                } catch (EOFException e) {
                    sendResult(callback, eventList);
                } catch (FileNotFoundException e) {
                    sendFailed(callback, e);
                } catch (IOException e) {
                    sendFailed(callback, e);
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

    private static void sendResult(final TLogParserCallback callback, final List<Event> events) {
        if (callback != null) {
            callback.onResult(events);
        }
    }

    private static void sendFailed(final TLogParserCallback callback, final Exception e) {
        if (callback != null) {
            callback.onFailed(e);
        }
    }

    private static Event next(DataInputStream in) throws IOException {
        long timestamp = in.readLong() / 1000;
        MAVLinkPacket packet;
        while ((packet = parser.mavlink_parse_char(in.readUnsignedByte())) == null) ;
        return new Event(timestamp, packet.unpack());
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

    /**
     * Callback for asynchronous TLog iterator.
     */
    public interface TLogIteratorCallback {
        /**
         * Next message was retrieved successfully with Event value
         *
         * @param event
         */
        void onResult(Event event);

        /**
         * Next message was not retrieved.
         * When no message exists with specified paramater, NoSuchElementException is returned.
         *
         * @param e
         */
        void onFailed(Exception e);
    }

    /**
     * Callback for asynchronous TLog parser.
     */
    public interface TLogParserCallback {
        /**
         * Next message was retrieved successfully with Event value
         *
         * @param events
         */
        void onResult(List<Event> events);

        /**
         * Next message was not retrieved.
         * When no message exists with specified paramater, NoSuchElementException is returned.
         *
         * @param e
         */
        void onFailed(Exception e);
    }

    /**
     * Filter class for TLog iterator to allow the caller to determine the criteria for returned event.
     */
    public interface TLogIteratorFilter {
        /**
         * This method is called when an event is parsed to determine whether the caller wants this result.
         *
         * @param event
         * @return whether this event should be accepted based off criteria
         */
        boolean acceptEvent(Event event);
    }

    /**
     * Filter class for TLog parser to allow the caller to determine the criteria for the list
     * of returned events.
     */
    public interface TLogParserFilter {
        /**
         * This method is called when an event is parsed to determine whether the caller wants this result
         * in the returned list.
         *
         * @param e
         * @return whether this event should be accepted based off criteria
         */
        boolean addEventToList(Event e);

        /**
         * This method when iterating the TLog file to determine whether the caller wants to
         * continue iterating.
         *
         * @return whether to continue iterating or stop and send results
         */
        boolean continueIterating();
    }
}

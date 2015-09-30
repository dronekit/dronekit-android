package com.o3dr.android.client.data.tlog;

import android.os.Handler;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Parser;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Parse TLog file into Events
 */
public class TLogParser {
    private static final int MSGFILTER_NONE = -1;

    private File file;
    private DataInputStream in = null;
    private final Parser parser = new Parser();
    private ExecutorService parseTlogExecutor;
    private final Handler handler;

    /**
     * Constructor to create a TLogParser with new Handler.
     *
     * @param uri Location of the TLog files
     */
    public TLogParser(URI uri) {
        this(uri, new Handler());
    }

    /**
     * Constructor to create a TLogParser with a specified Handler.
     *
     * @param uri  Location of the TLog files
     * @param handler Handler to post results to
     */
    public TLogParser(URI uri, Handler handler) {
        this.handler = handler;
        file = new File(uri.toString());
    }

    /**
     * Opens TLog file to begin parsing.
     *
     * @throws FileNotFoundException
     */
    public void startParse() throws FileNotFoundException {
        parseTlogExecutor = Executors.newSingleThreadExecutor();
        in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
    }

    /**
     * Closes TLog file
     *
     * @throws IOException
     */
    public void finishParse() throws IOException {
        parseTlogExecutor.shutdownNow();
        in.close();
    }

    /**
     * Retrieve next message from TLog file asynchronously.
     * startParse must be called before this method.
     *
     * @param callback
     */
    public void nextAsync(final TLogParseCallback callback) {
        nextAsync(MSGFILTER_NONE, callback);
    }

    /**
     * Retrieve next message with specified message filter from TLog file asynchronously.
     * startParse must be called before this method.
     *
     * @param msgFilter
     * @param callback
     */
    public void nextAsync(final int msgFilter, final TLogParseCallback callback) {
        parseTlogExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Event event = next(msgFilter);
                    sendResult(callback, event);
                } catch (IOException e) {
                    sendFailed(callback, e);
                }
            }
        });
    }

    private Event next(int msgFilter) throws IOException {
        while (in.available() > 0) {
            long timestamp = in.readLong() / 1000;
            MAVLinkPacket packet;
            while ((packet = parser.mavlink_parse_char(in.readUnsignedByte())) == null) ;

            if (msgFilter == MSGFILTER_NONE || packet.msgid == msgFilter) {
                return new Event(timestamp, packet.unpack());
            }
        }
        return null;
    }

    private void sendResult(final TLogParseCallback callback, final Event event) {
        if (callback != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onResult(event);
                }
            });
        }
    }

    private void sendFailed(final TLogParseCallback callback, final Exception e) {
        if (callback != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onFailed(e);
                }
            });
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

    /**
     * Callback for asynchronous TLog parsing.
     */
    public interface TLogParseCallback {
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
}

package com.o3dr.android.client.data.tlog.callback;

import com.o3dr.android.client.data.tlog.TLogParser;

import java.util.List;

/**
 * Callback for asynchronous TLog parser.
 */
public interface TLogParserCallback {
    /**
     * Next message was retrieved successfully with Event value
     *
     * @param events
     */
    void onResult(List<TLogParser.Event> events);

    /**
     * Next message was not retrieved.
     * When no message exists with specified paramater, NoSuchElementException is returned.
     *
     * @param e
     */
    void onFailed(Exception e);
}
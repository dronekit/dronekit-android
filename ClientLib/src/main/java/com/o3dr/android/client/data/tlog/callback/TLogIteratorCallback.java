package com.o3dr.android.client.data.tlog.callback;

import com.o3dr.android.client.data.tlog.TLogParser;

/**
 * Callback for asynchronous TLog iterator.
 */
public interface TLogIteratorCallback {

    /**
     * Next message was retrieved successfully with Event value
     *
     * @param event
     */
    void onResult(TLogParser.Event event);

    /**
     * Next message was not retrieved.
     * When no message exists with specified paramater, NoSuchElementException is returned.
     *
     * @param e
     */
    void onFailed(Exception e);
}
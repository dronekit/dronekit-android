package com.o3dr.android.client.data.tlog.callback;

import com.o3dr.android.client.data.tlog.TLogParser;

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
    boolean addEventToList(TLogParser.Event e);

    /**
     * This method when iterating the TLog file to determine whether the caller wants to
     * continue iterating.
     *
     * @return whether to continue iterating or stop and send results
     */
    boolean continueIterating();
}
package com.o3dr.services.android.lib.data;

/**
 * The contract between the 3DR Services data provider, and applications. Contains definitions for the supported URIs
 * and columns.
 */
public class ServiceDataContract {

    private static final String PACKAGE_NAME = "com.o3dr.services.android.provider";

    /**
     * Authority for the service data provider.
     */
    public static final String DATA_PROVIDER_AUTHORITY = PACKAGE_NAME + ".dataprovider";

    /**
     * Authority for the file provider.
     */
    public static final String FILE_PROVIDER_AUTHORITY = PACKAGE_NAME + ".fileprovider";

    /**
     * Action used to request a tlog file.
     */
    public static final String ACTION_REQUEST_TLOG = PACKAGE_NAME + ".action.REQUEST_TLOG_FILE";

    /**
     * Action used to notify of the availability of a tlog file.
     */
    public static final String ACTION_RETURN_TLOG = PACKAGE_NAME + ".action.RETURN_TLOG_FILE";

    /**
     * Mime type for a tlog file.
     */
    public static final String TLOG_MIME_TYPE = "application/octet-stream";
}

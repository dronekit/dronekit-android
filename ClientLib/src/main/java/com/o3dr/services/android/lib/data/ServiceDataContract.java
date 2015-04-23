package com.o3dr.services.android.lib.data;

import com.o3dr.services.android.lib.util.Utils;

/**
 * The contract between the 3DR Services data provider, and applications. Contains definitions for the supported URIs
 * and columns.
 */
public class ServiceDataContract {

    private static final String PACKAGE_NAME = Utils.PACKAGE_NAME + ".provider";

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
     * Key used to access the id of the app requesting the tlog data.
     */
    public static final String EXTRA_REQUEST_TLOG_APP_ID = PACKAGE_NAME + ".extra.REQUEST_TLOG_APP_ID";

    /**
     * Key used to access the file path for the request tlog data.
     */
    public static final String EXTRA_TLOG_ABSOLUTE_PATH = "extra_tlog_absolute_path";

    /**
     * Action used to notify of the availability of a tlog file.
     */
    public static final String ACTION_RETURN_TLOG = PACKAGE_NAME + ".action.RETURN_TLOG_FILE";

    /**
     * Mime type for a tlog file.
     */
    public static final String TLOG_MIME_TYPE = "application/octet-stream";
}

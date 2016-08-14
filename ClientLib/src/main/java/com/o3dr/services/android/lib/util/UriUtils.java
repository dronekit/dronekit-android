package com.o3dr.services.android.lib.util;

import android.net.Uri;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by fhuya on 8/13/2016.
 */
public class UriUtils {

    private UriUtils() {
    }

    /**
     * Retrieves an output stream from the given uri.
     * For now, this can only handle file uri.
     *
     * @param uri
     * @return
     * @since 3.0.0
     */
    public static OutputStream getOutputStream(Uri uri) throws IOException {
        return new FileOutputStream(uri.getPath());
    }

    /**
     * Retrieves an input stream from the given uri.
     * For now this can only handle file uri.
     *
     * @param uri
     * @return
     * @since 3.0.0
     */
    public static InputStream getInputStream(Uri uri) throws IOException {
        return new FileInputStream(uri.getPath());
    }
}

package org.droidplanner.services.android.utils.video;

/**
 * Created by fhuya on 12/4/14.
 */
public interface DecoderListener {

    void onDecodingStarted();

    boolean wantDecoderInput();

    void onDecodingError();

    void onDecoderInput(byte[] bytes, int validLength);

    void onDecodingEnded();

}

package org.droidplanner.services.android.utils.video;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import timber.log.Timber;

/**
 * Created by Fredia Huya-Kouadio on 2/19/15.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MediaCodecManager {

    private static final String TAG = MediaCodecManager.class.getSimpleName();

    private static final String MIME_TYPE = "video/avc";
    public static final int DEFAULT_VIDEO_WIDTH = 1920;
    public static final int DEFAULT_VIDEO_HEIGHT = 1080;

    private interface ISimpleByteBuffer
    {
        void put(ByteBuffer inBuffer);
        void put(byte[] array, int offset, int validbytes);
        void clear();
        byte[] array();
        void order(ByteOrder order);
        int size();
    }
    public static class ByteBufferWrapper implements ISimpleByteBuffer
    {
        private ByteBuffer mBuffer;
        public ByteBufferWrapper(ByteBuffer byteBuffer)
        {
            mBuffer = byteBuffer;
        }
        public void put(ByteBuffer inBuffer)
        {
            mBuffer.put(inBuffer);
        }
        public void put(byte[] array, int offset, int validBytes)
        {
            mBuffer.put(array, offset, validBytes);
        }
        public void clear()
        {
            mBuffer.clear();
        }
        public byte[] array()
        {
            return mBuffer.array();
        }
        public void order(ByteOrder order)
        {
            mBuffer.order();
        }
        public int size()
        {
            return mBuffer.limit() - mBuffer.position();
        }
    }
    private static class GrowBuffer implements ISimpleByteBuffer
    {
        private ByteBuffer mBuffer = null;

        private void growToFit(int size)
        {
            int avail = mBuffer.remaining();
            if (avail < size) {
                ByteBuffer newByteBuffer = ByteBuffer.allocate(Math.max(mBuffer.capacity()*2, mBuffer.capacity() + size));
                mBuffer.flip();
                newByteBuffer.put(mBuffer);
                mBuffer = newByteBuffer;
            }
        }

        public GrowBuffer(int baseSize)
        {
            mBuffer = ByteBuffer.allocate(baseSize);
        }

        public void put(ByteBuffer inBuffer)
        {
            growToFit(inBuffer.limit() - inBuffer.position());
            mBuffer.put(inBuffer);
        }
        public void put(byte[] array, int offset, int validBytes)
        {
            growToFit(validBytes-offset);
            mBuffer.put(array, offset, validBytes);
        }

        public void clear()
        {
            mBuffer.clear();
        }

        public byte[] array()
        {
            return mBuffer.array();
        }
        public void order(ByteOrder order)
        {
            mBuffer.order();
        }
        public int size()
        {
            return mBuffer.limit() - mBuffer.position();
        }
    }

    private GrowBuffer mGrowBuffer = null;

    private final Runnable stopSafely = new Runnable() {
        @Override
        public void run() {
            processInputData.set(false);
            sendCompletionFlag.set(false);
            naluChunkAssembler.reset();

            if (dequeueRunner != null && dequeueRunner.isAlive()) {
                Log.d(TAG, "Interrupting dequeue runner thread.");
                dequeueRunner.interrupt();
            }
            dequeueRunner = null;

            final MediaCodec mediaCodec = mediaCodecRef.get();
            if (mediaCodec != null) {
                try {
                    mediaCodec.stop();
                }catch(IllegalStateException e){
                    Log.e(TAG, "Error while stopping media codec.", e);
                }
                mediaCodec.release();
                mediaCodecRef.set(null);
            }

            surfaceRef.set(null);

            isDecoding.set(false);
            handler.post(decodingEndedNotification);
        }
    };

    private final Runnable decodingStartedNotification = new Runnable() {
        @Override
        public void run() {
            final DecoderListener listener = decoderListenerRef.get();
            if (listener != null)
                listener.onDecodingStarted();
        }
    };

    private final Runnable decodingErrorNotification = new Runnable() {
        @Override
        public void run() {
            final DecoderListener listener = decoderListenerRef.get();
            if (listener != null)
                listener.onDecodingError();
        }
    };

    private final Runnable decodingEndedNotification = new Runnable() {
        @Override
        public void run() {
            final DecoderListener listener = decoderListenerRef.get();
            if (listener != null)
                listener.onDecodingEnded();
        }
    };

    private final AtomicBoolean decodedFirstFrame = new AtomicBoolean(false);

    private final AtomicBoolean isDecoding = new AtomicBoolean(false);
    private final AtomicBoolean processInputData = new AtomicBoolean(false);
    private final AtomicBoolean sendCompletionFlag = new AtomicBoolean(false);
    private final AtomicReference<Surface> surfaceRef = new AtomicReference<>();
    private final AtomicReference<MediaCodec> mediaCodecRef = new AtomicReference<>();
    private final AtomicReference<DecoderListener> decoderListenerRef = new AtomicReference<>();
    private final NALUChunkAssembler naluChunkAssembler;

    private final Handler handler;

    private DequeueCodec dequeueRunner;


    public MediaCodecManager(Handler handler) {
        this.handler = handler;
        this.naluChunkAssembler = new NALUChunkAssembler();
    }

    Surface getSurface(){
        return surfaceRef.get();
    }

    public void startDecoding(Surface surface, DecoderListener listener) throws IOException {
        if (surface == null && !listener.wantDecoderInput())
            throw new IllegalStateException("Surface argument must be non-null unless a listener is registered.");

        if (isDecoding.compareAndSet(false, true)) {
            Log.i(TAG, "Starting decoding...");
            this.naluChunkAssembler.reset();

            this.decoderListenerRef.set(listener);

            if (surface != null)
            {
                final MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, DEFAULT_VIDEO_WIDTH, DEFAULT_VIDEO_HEIGHT);

                final MediaCodec mediaCodec = MediaCodec.createDecoderByType(MIME_TYPE);
                mediaCodec.configure(mediaFormat, surface, null, 0);
                mediaCodec.start();

                mediaCodecRef.set(mediaCodec);
            } else {
                mediaCodecRef.set(null);
            }
            surfaceRef.set(surface);
            processInputData.set(true);

            if (surface != null)
            {
                dequeueRunner = new DequeueCodec();
                dequeueRunner.start();
            }
        }
    }

    public void stopDecoding(DecoderListener listener) {
        Log.i(TAG, "Stopping input data processing...");

        this.decoderListenerRef.set(listener);
        if(!isDecoding.get()) {
            if (listener != null) {
                    notifyDecodingEnded();
            }
        }
        else {
            if(decodedFirstFrame.get()) {
                if (processInputData.compareAndSet(true, false)) {
                    sendCompletionFlag.set(!processNALUChunk(naluChunkAssembler.getEndOfStream()));
                }
            }
            else{
                handler.post(stopSafely);
            }
        }
    }

    public void onInputDataReceived(byte[] data, int dataSize) {
        if (isDecoding.get()) {
            if (processInputData.get())
            {
                NALUChunk naluChunk = naluChunkAssembler.assembleNALUChunk(data, dataSize);
                if (naluChunk != null)
                    processNALUChunk(naluChunk);
            } else {
                if (sendCompletionFlag.get()) {
                    Log.d(TAG, "Sending end of stream data.");
                    sendCompletionFlag.set(!processNALUChunk(naluChunkAssembler.getEndOfStream()));                }
            }
        }
    }

    private boolean processNALUChunk(NALUChunk naluChunk) {
        if (naluChunk == null)
            return false;

        final MediaCodec mediaCodec = mediaCodecRef.get();
        final DecoderListener decListener = decoderListenerRef.get();

        if (decListener == null)
        {
            return false;
        }

        final boolean decListenerWantsDecoderInput = decListener.wantDecoderInput();

        if (mediaCodec == null && !decListenerWantsDecoderInput)
        {
            return false;
        }

        if (mGrowBuffer == null && decListenerWantsDecoderInput)
        {
            mGrowBuffer = new GrowBuffer(1024 * 64);
        }

        try {
            final int index = mediaCodec == null ?
                              0 :
                              mediaCodec.dequeueInputBuffer(-1);
            if (index >= 0) {
                ISimpleByteBuffer inputBuffer=null;
                ISimpleByteBuffer mediaCodecInBuffer = null;

                if (mediaCodec != null)
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mediaCodecInBuffer = new ByteBufferWrapper( mediaCodec.getInputBuffer(index) );
                    } else {
                        mediaCodecInBuffer = new ByteBufferWrapper( mediaCodec.getInputBuffers()[index] );
                    }
                }

                if (decListenerWantsDecoderInput) {
                    inputBuffer = mGrowBuffer;
                }
                else {
                    inputBuffer = mediaCodecInBuffer;
                }

                if (inputBuffer == null)
                    return false;

                inputBuffer.clear();
                int totalLength = 0;

                int payloadCount = naluChunk.payloads.length;
                for (int i = 0; i < payloadCount; i++) {
                    ByteBuffer payload = naluChunk.payloads[i];

                    if (payload.capacity() == 0)
                        continue;

                    inputBuffer.order(payload.order());
                    final int dataLength = payload.position();

                    inputBuffer.put(payload.array(), 0, dataLength);

                    totalLength += dataLength;
                }

                if (mediaCodec != null) {
                    if (inputBuffer != mediaCodecInBuffer) {
                        mediaCodecInBuffer.clear();
                        mediaCodecInBuffer.put(inputBuffer.array(), 0, totalLength);
                    }

                    mediaCodec.queueInputBuffer(index, 0, totalLength, naluChunk.presentationTime, naluChunk.flags);
                }
                if (decListenerWantsDecoderInput)
                    decListener.onDecoderInput(inputBuffer.array(), totalLength);
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, e.getMessage(), e);
            return false;
        }

        return true;
    }

    private void notifyDecodingStarted() {
        handler.post(decodingStartedNotification);
    }

    private void notifyDecodingError() {
        handler.post(decodingErrorNotification);
    }

    private void notifyDecodingEnded() {
        handler.post(stopSafely);
    }

    private class DequeueCodec extends Thread {
        @Override
        public void run() {
            final MediaCodec mediaCodec = mediaCodecRef.get();
            if (mediaCodec == null)
                throw new IllegalStateException("Start decoding hasn't been called yet.");

            Log.i(TAG, "Starting dequeue codec runner.");

            final MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            decodedFirstFrame.set(false);
            boolean doRender;
            boolean continueDequeue = true;
            try {
                while (continueDequeue) {
                    final int index = mediaCodec.dequeueOutputBuffer(info, -1);
                    if (index >= 0) {
                        doRender = info.size != 0;
                        mediaCodec.releaseOutputBuffer(index, doRender);

                        if (decodedFirstFrame.compareAndSet(false, true)) {
                            notifyDecodingStarted();
                            Log.i(TAG, "Received first decoded frame of size " + info.size);
                        }

                        continueDequeue = (info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) == 0;
                        if (!continueDequeue) {
                            Log.i(TAG, "Received end of stream flag.");
                        }
                    }
                }
            } catch (IllegalStateException e) {
                if(!isInterrupted()) {
                    Log.e(TAG, "Decoding error!", e);
                    notifyDecodingError();
                }
            } finally {
                if (!isInterrupted())
                    notifyDecodingEnded();
                Log.i(TAG, "Stopping dequeue codec runner.");
            }
        }
    }
}

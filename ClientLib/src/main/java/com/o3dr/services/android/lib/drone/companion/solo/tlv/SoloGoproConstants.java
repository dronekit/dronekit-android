package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.MAVLink.enums.GOPRO_CAPTURE_MODE.GOPRO_CAPTURE_MODE_BURST;
import static com.MAVLink.enums.GOPRO_CAPTURE_MODE.GOPRO_CAPTURE_MODE_MULTI_SHOT;
import static com.MAVLink.enums.GOPRO_CAPTURE_MODE.GOPRO_CAPTURE_MODE_PHOTO;
import static com.MAVLink.enums.GOPRO_CAPTURE_MODE.GOPRO_CAPTURE_MODE_PLAYBACK;
import static com.MAVLink.enums.GOPRO_CAPTURE_MODE.GOPRO_CAPTURE_MODE_SETUP;
import static com.MAVLink.enums.GOPRO_CAPTURE_MODE.GOPRO_CAPTURE_MODE_TIME_LAPSE;
import static com.MAVLink.enums.GOPRO_CAPTURE_MODE.GOPRO_CAPTURE_MODE_VIDEO;
import static com.MAVLink.enums.GOPRO_COMMAND.GOPRO_COMMAND_CAPTURE_MODE;
import static com.MAVLink.enums.GOPRO_COMMAND.GOPRO_COMMAND_CHARGING;
import static com.MAVLink.enums.GOPRO_COMMAND.GOPRO_COMMAND_LOW_LIGHT;
import static com.MAVLink.enums.GOPRO_COMMAND.GOPRO_COMMAND_PHOTO_BURST_RATE;
import static com.MAVLink.enums.GOPRO_COMMAND.GOPRO_COMMAND_PHOTO_RESOLUTION;
import static com.MAVLink.enums.GOPRO_COMMAND.GOPRO_COMMAND_POWER;
import static com.MAVLink.enums.GOPRO_COMMAND.GOPRO_COMMAND_PROTUNE;
import static com.MAVLink.enums.GOPRO_COMMAND.GOPRO_COMMAND_PROTUNE_COLOUR;
import static com.MAVLink.enums.GOPRO_COMMAND.GOPRO_COMMAND_PROTUNE_EXPOSURE;
import static com.MAVLink.enums.GOPRO_COMMAND.GOPRO_COMMAND_PROTUNE_GAIN;
import static com.MAVLink.enums.GOPRO_COMMAND.GOPRO_COMMAND_PROTUNE_SHARPNESS;
import static com.MAVLink.enums.GOPRO_COMMAND.GOPRO_COMMAND_PROTUNE_WHITE_BALANCE;
import static com.MAVLink.enums.GOPRO_COMMAND.GOPRO_COMMAND_SHUTTER;
import static com.MAVLink.enums.GOPRO_COMMAND.GOPRO_COMMAND_TIME;
import static com.MAVLink.enums.GOPRO_COMMAND.GOPRO_COMMAND_VIDEO_SETTINGS;

/**
 * Stores the gopro constants
 * Created by Fredia Huya-Kouadio on 10/15/15.
 *
 * @since 2.6.8
 */
public class SoloGoproConstants {

    //Private constructor to prevent instantiation.
    private SoloGoproConstants(){}


    @IntDef({POWER, CAPTURE_MODE, GOPRO_COMMAND_SHUTTER, GOPRO_COMMAND_VIDEO_SETTINGS,
            GOPRO_COMMAND_LOW_LIGHT, GOPRO_COMMAND_PHOTO_RESOLUTION, GOPRO_COMMAND_PHOTO_BURST_RATE,
            GOPRO_COMMAND_PROTUNE, GOPRO_COMMAND_PROTUNE_WHITE_BALANCE, GOPRO_COMMAND_PROTUNE_COLOUR,
            GOPRO_COMMAND_PROTUNE_GAIN, GOPRO_COMMAND_PROTUNE_SHARPNESS, GOPRO_COMMAND_PROTUNE_EXPOSURE,
            GOPRO_COMMAND_TIME, GOPRO_COMMAND_CHARGING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RequestCommand{}

    public static final short POWER = GOPRO_COMMAND_POWER;
    public static final short CAPTURE_MODE = GOPRO_COMMAND_CAPTURE_MODE;


    @IntDef({STOP_RECORDING, START_RECORDING, TOGGLE_RECORDING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RecordCommand{}

    public static final int STOP_RECORDING = 0;
    public static final int START_RECORDING = 1;
    public static final int TOGGLE_RECORDING = 2;

    @IntDef({STATUS_NO_GOPRO, STATUS_INCOMPATIBLE_GOPRO, STATUS_GOPRO_CONNECTED, STATUS_ERROR_OVER_TEMPERATURE, STATUS_ERROR_NO_STORAGE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface GoproStatus{}

    public static final byte STATUS_NO_GOPRO = 0;
    public static final byte STATUS_INCOMPATIBLE_GOPRO = 1;
    public static final byte STATUS_GOPRO_CONNECTED = 2;
    public static final byte STATUS_ERROR_OVER_TEMPERATURE = 3;
    public static final byte STATUS_ERROR_NO_STORAGE = 4;


    @IntDef({RECORDING_OFF, RECORDING_ON})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RecordingStatus{}

    public static final byte RECORDING_OFF = 0;
    public static final byte RECORDING_ON = 1;


    @IntDef({CAPTURE_MODE_VIDEO, CAPTURE_MODE_PHOTO, CAPTURE_MODE_BURST, CAPTURE_MODE_TIME_LAPSE,
            CAPTURE_MODE_MULTI_SHOT, CAPTURE_MODE_PLAYBACK, CAPTURE_MODE_SETUP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CaptureMode{}

    /**
     * Camera video mode.
     */
    public static final byte CAPTURE_MODE_VIDEO = GOPRO_CAPTURE_MODE_VIDEO;

    /**
     * Camera photo mode.
     */
    public static final byte CAPTURE_MODE_PHOTO = GOPRO_CAPTURE_MODE_PHOTO;

    /**
     * Camera photo burst mode.
     */
    public static final byte CAPTURE_MODE_BURST = GOPRO_CAPTURE_MODE_BURST;

    /**
     * Camera time lapse mode.
     */
    public static final byte CAPTURE_MODE_TIME_LAPSE = GOPRO_CAPTURE_MODE_TIME_LAPSE;

    public static final byte CAPTURE_MODE_MULTI_SHOT = GOPRO_CAPTURE_MODE_MULTI_SHOT;
    public static final byte CAPTURE_MODE_PLAYBACK = GOPRO_CAPTURE_MODE_PLAYBACK;
    public static final byte CAPTURE_MODE_SETUP = GOPRO_CAPTURE_MODE_SETUP;
}

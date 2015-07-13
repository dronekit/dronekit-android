package com.o3dr.services.android.lib.drone.companion.solo;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Defines the available controller modes.
 * Created by Fredia Huya-Kouadio on 7/13/15.
 */
public class SoloControllerMode {

    @IntDef({MODE_1, MODE_2})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ControllerMode {}

    /**
     *
     */
    public static final int MODE_1 = 1;
    public static final int MODE_2 = 2;

    //Private to prevent instantiation.
    private SoloControllerMode(){}
}

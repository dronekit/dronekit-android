package org.droidplanner.services.android.drone.companion.solo.sololink.tlv;

import java.nio.ByteBuffer;

/**
 * Created by Fredia Huya-Kouadio on 5/24/15.
 */
public class SoloFollowOptions extends SoloShotOptions {

    private static final String TAG = SoloFollowOptions.class.getSimpleName();

    private static final int LOOK_AT_ENABLED_VALUE = 1;
    private static final int LOOK_AT_DISABLED_VALUE = 0;

    private boolean lookAt;

    public SoloFollowOptions(float cruiseSpeed, boolean lookAt){
        super(TLVMessageTypes.TYPE_SOLO_FOLLOW_OPTIONS, 8, cruiseSpeed);
        this.lookAt = lookAt;
    }

    public SoloFollowOptions(){
        this(PAUSED_CRUISE_SPEED, false);
    }

    SoloFollowOptions(float cruiseSpeed, int lookAtValue){
        this(cruiseSpeed, lookAtValue == LOOK_AT_ENABLED_VALUE);
    }

    public boolean isLookAt() {
        return lookAt;
    }

    public void setLookAt(boolean lookAt) {
        this.lookAt = lookAt;
    }

    @Override
    protected void getMessageValue(ByteBuffer valueCarrier){
        super.getMessageValue(valueCarrier);
        valueCarrier.putInt(lookAt ? LOOK_AT_ENABLED_VALUE : LOOK_AT_DISABLED_VALUE);
    }

}

package org.droidplanner.services.android.core.drone.autopilot;

import com.o3dr.services.android.lib.drone.property.DroneAttribute;
import com.o3dr.services.android.lib.model.ICommandListener;
import com.o3dr.services.android.lib.model.action.Action;

/**
 * Created by Fredia Huya-Kouadio on 7/27/15.
 */
public interface Drone {

    boolean isConnected();

    DroneAttribute getAttribute(String attributeType);

    boolean executeAsyncAction(Action action, ICommandListener listener);
}

package org.droidplanner.services.android.impl.core.mission;

import com.o3dr.services.android.lib.coordinate.LatLong;

import org.droidplanner.services.android.impl.core.mission.commands.CameraTriggerImpl;
import org.droidplanner.services.android.impl.core.mission.commands.ChangeSpeedImpl;
import org.droidplanner.services.android.impl.core.mission.commands.ConditionYawImpl;
import org.droidplanner.services.android.impl.core.mission.commands.DoJumpImpl;
import org.droidplanner.services.android.impl.core.mission.commands.EpmGripperImpl;
import org.droidplanner.services.android.impl.core.mission.commands.LoiterTimeImpl;
import org.droidplanner.services.android.impl.core.mission.commands.LoiterToAltImpl;
import org.droidplanner.services.android.impl.core.mission.commands.RawMissionCommandImpl;
import org.droidplanner.services.android.impl.core.mission.commands.ReturnToHomeImpl;
import org.droidplanner.services.android.impl.core.mission.commands.SetRelayImpl;
import org.droidplanner.services.android.impl.core.mission.commands.SetServoImpl;
import org.droidplanner.services.android.impl.core.mission.commands.TakePictureImpl;
import org.droidplanner.services.android.impl.core.mission.commands.TakeoffImpl;
import org.droidplanner.services.android.impl.core.mission.survey.SplineSurveyImpl;
import org.droidplanner.services.android.impl.core.mission.survey.SurveyImpl;
import org.droidplanner.services.android.impl.core.mission.waypoints.CircleImpl;
import org.droidplanner.services.android.impl.core.mission.waypoints.DoLandStartImpl;
import org.droidplanner.services.android.impl.core.mission.waypoints.LandImpl;
import org.droidplanner.services.android.impl.core.mission.waypoints.RegionOfInterestImpl;
import org.droidplanner.services.android.impl.core.mission.waypoints.SplineWaypointImpl;
import org.droidplanner.services.android.impl.core.mission.waypoints.StructureScannerImpl;
import org.droidplanner.services.android.impl.core.mission.waypoints.WaypointImpl;

import java.util.Collections;

public enum MissionItemType {
    WAYPOINT("Waypoint"),
    SPLINE_WAYPOINT("Spline Waypoint"),
    TAKEOFF("Takeoff"),
    RTL("Return to Launch"),
    LAND("Land"),
    CIRCLE("Circle"),
    ROI("Region of Interest"),
    SURVEY("Survey"),
    SPLINE_SURVEY("Spline Survey"),
    CYLINDRICAL_SURVEY("Structure Scan"),
    CHANGE_SPEED("Change Speed"),
    CAMERA_TRIGGER("Camera Trigger"),
    EPM_GRIPPER("EPM"),
    SET_SERVO("Set Servo"),
    CONDITION_YAW("Set Yaw"),
    SET_RELAY("Set Relay"),
    DO_LAND_START("Do Land Start"),
    DO_JUMP("Do Jump"),
    LOITER_TO_ALT("Loiter to Alt"),
    LOITER_TIME("Loiter Time"),
    TAKE_PICTURE("Take Picture"),
    VTOL_TAKEOFF("VTOL Takeoff"),
    VTOL_TRANSITION("VTOL Transition"),
    VTOL_LAND("VTOL Land"),
    RAW_COMMAND("Raw Command")
    ;

    private final String name;

    private MissionItemType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public MissionItemImpl getNewItem(MissionItemImpl referenceItem) throws IllegalArgumentException {
        switch (this) {
            case WAYPOINT:
                return new WaypointImpl(referenceItem);
            case SPLINE_WAYPOINT:
                return new SplineWaypointImpl(referenceItem);
            case TAKEOFF:
                return new TakeoffImpl(referenceItem);
            case CHANGE_SPEED:
                return new ChangeSpeedImpl(referenceItem);
            case LOITER_TO_ALT:
                return new LoiterToAltImpl(referenceItem);
            case LOITER_TIME:
                return new LoiterTimeImpl(referenceItem);
            case TAKE_PICTURE:
                return new TakePictureImpl(referenceItem);
            case CAMERA_TRIGGER:
                return new CameraTriggerImpl(referenceItem);
            case EPM_GRIPPER:
                return new EpmGripperImpl(referenceItem);
            case RTL:
                return new ReturnToHomeImpl(referenceItem);
            case LAND:
                return new LandImpl(referenceItem);
            case CIRCLE:
                return new CircleImpl(referenceItem);
            case ROI:
                return new RegionOfInterestImpl(referenceItem);
            case SURVEY:
                return new SurveyImpl(referenceItem.getMission(), Collections.<LatLong>emptyList());
            case SPLINE_SURVEY:
                return new SplineSurveyImpl(referenceItem.getMission(), Collections.<LatLong>emptyList());
            case CYLINDRICAL_SURVEY:
                return new StructureScannerImpl(referenceItem);
            case SET_SERVO:
                return new SetServoImpl(referenceItem);
            case CONDITION_YAW:
                return new ConditionYawImpl(referenceItem);
            case SET_RELAY:
                return new SetRelayImpl(referenceItem);
            case DO_LAND_START:
                return new DoLandStartImpl(referenceItem);
            case DO_JUMP:
                return new DoJumpImpl(referenceItem);
            case RAW_COMMAND:
                return new RawMissionCommandImpl(referenceItem);
            default:
                throw new IllegalArgumentException("Unrecognized mission item type (" + name + ")" + "");
        }
    }
}

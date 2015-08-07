package org.droidplanner.services.android.core.mission;

import org.droidplanner.services.android.core.helpers.coordinates.Coord2D;
import org.droidplanner.services.android.core.mission.commands.CameraTrigger;
import org.droidplanner.services.android.core.mission.commands.ChangeSpeed;
import org.droidplanner.services.android.core.mission.commands.ConditionYaw;
import org.droidplanner.services.android.core.mission.commands.EpmGripper;
import org.droidplanner.services.android.core.mission.commands.DoJumpImpl;
import org.droidplanner.services.android.core.mission.commands.ReturnToHome;
import org.droidplanner.services.android.core.mission.commands.SetRelayImpl;
import org.droidplanner.services.android.core.mission.commands.SetServo;
import org.droidplanner.services.android.core.mission.commands.Takeoff;
import org.droidplanner.services.android.core.mission.survey.SplineSurveyImpl;
import org.droidplanner.services.android.core.mission.survey.SurveyImpl;
import org.droidplanner.services.android.core.mission.waypoints.Circle;
import org.droidplanner.services.android.core.mission.waypoints.DoLandStartImpl;
import org.droidplanner.services.android.core.mission.waypoints.Land;
import org.droidplanner.services.android.core.mission.waypoints.RegionOfInterest;
import org.droidplanner.services.android.core.mission.waypoints.SplineWaypoint;
import org.droidplanner.services.android.core.mission.waypoints.StructureScanner;
import org.droidplanner.services.android.core.mission.waypoints.Waypoint;

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
    DO_JUMP("Do Jump");

    private final String name;

    private MissionItemType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public MissionItem getNewItem(MissionItem referenceItem) throws IllegalArgumentException {
        switch (this) {
            case WAYPOINT:
                return new Waypoint(referenceItem);
            case SPLINE_WAYPOINT:
                return new SplineWaypoint(referenceItem);
            case TAKEOFF:
                return new Takeoff(referenceItem);
            case CHANGE_SPEED:
                return new ChangeSpeed(referenceItem);
            case CAMERA_TRIGGER:
                return new CameraTrigger(referenceItem);
            case EPM_GRIPPER:
                return new EpmGripper(referenceItem);
            case RTL:
                return new ReturnToHome(referenceItem);
            case LAND:
                return new Land(referenceItem);
            case CIRCLE:
                return new Circle(referenceItem);
            case ROI:
                return new RegionOfInterest(referenceItem);
            case SURVEY:
                return new SurveyImpl(referenceItem.getMission(), Collections.<Coord2D>emptyList());
            case SPLINE_SURVEY:
                return new SplineSurveyImpl(referenceItem.getMission(), Collections.<Coord2D>emptyList());
            case CYLINDRICAL_SURVEY:
                return new StructureScanner(referenceItem);
            case SET_SERVO:
                return new SetServo(referenceItem);
            case CONDITION_YAW:
                return new ConditionYaw(referenceItem);
            case SET_RELAY:
                return new SetRelayImpl(referenceItem);
            case DO_LAND_START:
                return new DoLandStartImpl(referenceItem);
            case DO_JUMP:
                return new DoJumpImpl(referenceItem);
            default:
                throw new IllegalArgumentException("Unrecognized mission item type (" + name + ")" + "");
        }
    }
}

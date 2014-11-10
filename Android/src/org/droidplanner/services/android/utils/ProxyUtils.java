package org.droidplanner.services.android.utils;

import com.ox3dr.services.android.lib.coordinate.LatLongAlt;
import org.droidplanner.core.mission.survey.CameraInfo;
import com.ox3dr.services.android.lib.drone.mission.item.MissionItem;
import com.ox3dr.services.android.lib.drone.mission.item.command.CameraTrigger;
import com.ox3dr.services.android.lib.drone.mission.item.command.ChangeSpeed;
import com.ox3dr.services.android.lib.drone.mission.item.command.EpmGripper;
import com.ox3dr.services.android.lib.drone.mission.item.command.ReturnToLaunch;
import com.ox3dr.services.android.lib.drone.mission.item.command.SetServo;
import com.ox3dr.services.android.lib.drone.mission.item.command.Takeoff;
import com.ox3dr.services.android.lib.drone.mission.item.complex.CameraDetail;
import com.ox3dr.services.android.lib.drone.mission.item.complex.StructureScanner;
import com.ox3dr.services.android.lib.drone.mission.item.complex.Survey;
import com.ox3dr.services.android.lib.drone.mission.item.complex.SurveyDetail;
import com.ox3dr.services.android.lib.drone.mission.item.spatial.Circle;
import com.ox3dr.services.android.lib.drone.mission.item.spatial.Land;
import com.ox3dr.services.android.lib.drone.mission.item.spatial.RegionOfInterest;
import com.ox3dr.services.android.lib.drone.mission.item.spatial.SplineWaypoint;
import com.ox3dr.services.android.lib.drone.mission.item.spatial.Waypoint;

import org.droidplanner.core.mission.commands.ReturnToHome;
import org.droidplanner.core.mission.survey.SurveyData;

/**
 * Created by fhuya on 11/10/14.
 */
public class ProxyUtils {

    public static CameraDetail getCameraDetail(CameraInfo camInfo){
        return new CameraDetail(camInfo.name, camInfo.sensorWidth,
                camInfo.sensorHeight, camInfo.sensorResolution, camInfo.focalLength,
                camInfo.overlap, camInfo.sidelap, camInfo.isInLandscapeOrientation);
    }

    public static SurveyDetail getSurveyDetail(SurveyData surveyData){
        SurveyDetail surveyDetail = new SurveyDetail();
        surveyDetail.setCameraDetail(getCameraDetail(surveyData.getCameraInfo()));
        surveyDetail.setSidelap(surveyData.getSidelap());
        surveyDetail.setOverlap(surveyData.getOverlap());
        surveyDetail.setAngle(surveyData.getAngle());
        surveyDetail.setAltitude(surveyData.getAltitude().valueInMeters());
        return surveyDetail;
    }

    public static MissionItem getProxyMissionItem(org.droidplanner.core.mission.MissionItem item){
        if(item == null)
            return null;

        MissionItem proxyMissionItem;
        switch(item.getType()){
            case WAYPOINT: {
                org.droidplanner.core.mission.waypoints.Waypoint source = (org.droidplanner.core.mission.waypoints.Waypoint) item;

                Waypoint temp = new Waypoint();
                temp.setCoordinate(MathUtils.coord3DToLatLongAlt(source.getCoordinate()));
                temp.setAcceptanceRadius(source.getAcceptanceRadius());
                temp.setDelay(source.getDelay());
                temp.setOrbitalRadius(source.getOrbitalRadius());
                temp.setOrbitCCW(source.isOrbitCCW());
                temp.setYawAngle(source.getYawAngle());

                proxyMissionItem = temp;
                break;
            }

            case SPLINE_WAYPOINT: {
                org.droidplanner.core.mission.waypoints.SplineWaypoint source = (org.droidplanner.core.mission.waypoints.SplineWaypoint) item;

                SplineWaypoint temp = new SplineWaypoint();
                temp.setCoordinate(MathUtils.coord3DToLatLongAlt(source.getCoordinate()));
                temp.setDelay(source.getDelay());

                proxyMissionItem = temp;
                break;
            }

            case TAKEOFF: {
                org.droidplanner.core.mission.commands.Takeoff source = (org.droidplanner.core.mission.commands.Takeoff) item;

                Takeoff temp = new Takeoff();
                temp.setTakeoffAltitude(source.getFinishedAlt().valueInMeters());

                proxyMissionItem = temp;
                break;
            }
            case RTL: {
                ReturnToHome source = (ReturnToHome) item;

                ReturnToLaunch temp = new ReturnToLaunch();
                temp.setReturnAltitude(source.getHeight().valueInMeters());

                proxyMissionItem = temp;
                break;
            }
            case LAND: {
                org.droidplanner.core.mission.waypoints.Land source = (org.droidplanner.core.mission.waypoints.Land) item;

                Land temp = new Land();
                temp.setCoordinate(MathUtils.coord3DToLatLongAlt(source.getCoordinate()));

                proxyMissionItem = temp;
                break;
            }

            case CIRCLE: {
                org.droidplanner.core.mission.waypoints.Circle source = (org.droidplanner.core.mission.waypoints.Circle) item;

                Circle temp = new Circle();
                temp.setCoordinate(MathUtils.coord3DToLatLongAlt(source.getCoordinate()));
                temp.setRadius(source.getRadius());
                temp.setTurns(source.getNumberOfTurns());

                proxyMissionItem = temp;
                break;
            }

            case ROI: {
                org.droidplanner.core.mission.waypoints.RegionOfInterest source = (org.droidplanner.core.mission.waypoints.RegionOfInterest) item;

                RegionOfInterest temp = new RegionOfInterest();
                temp.setCoordinate(MathUtils.coord3DToLatLongAlt(source.getCoordinate()));

                proxyMissionItem = temp;
                break;
            }

            case SURVEY: {
                org.droidplanner.core.mission.survey.Survey source = (org.droidplanner.core.mission.survey.Survey) item;

                boolean isValid = true;
                try{
                    source.build();
                }
                catch(Exception e){
                    isValid = false;
                }

                Survey temp = new Survey();
                temp.setValid(isValid);
                temp.setSurveyDetail(getSurveyDetail(source.surveyData));
                temp.setPolygonPoints(MathUtils.coord2DToLatLong(source.polygon.getPoints()));
                temp.setGridPoints(MathUtils.coord2DToLatLong(source.grid.gridPoints));
                temp.setCameraLocations(MathUtils.coord2DToLatLong(source.grid.getCameraLocations()));
                temp.setPolygonArea(source.polygon.getArea().valueInSqMeters());

                proxyMissionItem = temp;
                break;
            }

            case CYLINDRICAL_SURVEY: {
                org.droidplanner.core.mission.waypoints.StructureScanner source = (org.droidplanner.core.mission.waypoints.StructureScanner) item;

                StructureScanner temp = new StructureScanner();
                temp.setSurveyDetail(getSurveyDetail(source.getSurveyData()));
                temp.setCoordinate(MathUtils.coord3DToLatLongAlt(source.getCoordinate()));
                temp.setRadius(source.getRadius().valueInMeters());
                temp.setCrossHatch(source.isCrossHatchEnabled());
                temp.setHeightStep(source.getEndAltitude().valueInMeters());
                temp.setStepsCount(source.getNumberOfSteps());
                temp.setPath(MathUtils.coord2DToLatLong(source.getPath()));

                proxyMissionItem = temp;
                break;
            }
            case CHANGE_SPEED: {
                org.droidplanner.core.mission.commands.ChangeSpeed source = (org.droidplanner.core.mission.commands.ChangeSpeed) item;

                ChangeSpeed temp = new ChangeSpeed();
                temp.setSpeed(source.getSpeed().valueInMetersPerSecond());

                proxyMissionItem = temp;
                break;
            }

            case CAMERA_TRIGGER: {
                org.droidplanner.core.mission.commands.CameraTrigger source = (org.droidplanner.core.mission.commands.CameraTrigger) item;

                CameraTrigger temp = new CameraTrigger();
                temp.setTriggerDistance(source.getTriggerDistance().valueInMeters());

                proxyMissionItem = temp;
                break;
            }
            case EPM_GRIPPER: {
                org.droidplanner.core.mission.commands.EpmGripper source = (org.droidplanner.core.mission.commands.EpmGripper) item;

                EpmGripper temp = new EpmGripper();
                temp.setRelease(source.isRelease());

                proxyMissionItem = temp;
                break;
            }

            case SET_SERVO: {
                org.droidplanner.core.mission.commands.SetServo source = (org.droidplanner.core.mission.commands.SetServo) item;

                SetServo temp = new SetServo();
                temp.setChannel(source.getChannel());
                temp.setPwm(source.getPwm());

                proxyMissionItem = temp;
                break;
            }
            case CONDITION_YAW: {
                proxyMissionItem = null;
                break;
            }

            default:
                proxyMissionItem = null;
                break;
        }

        return proxyMissionItem;
    }
}

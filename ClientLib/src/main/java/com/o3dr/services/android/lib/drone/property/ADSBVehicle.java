package com.o3dr.services.android.lib.drone.property;

import android.content.Context;
import android.os.Parcel;

import com.MAVLink.common.msg_adsb_vehicle;
import com.MAVLink.enums.ADSB_EMITTER_TYPE;
import com.o3dr.android.client.R;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;

/**
 * An ADSB vehicle
 */
public class ADSBVehicle implements DroneAttribute {

    public enum Type {
        Unknown(0, 0)
    ,   Light(ADSB_EMITTER_TYPE.ADSB_EMITTER_TYPE_LIGHT, R.string.adsb_t_light)
    ,   Small(ADSB_EMITTER_TYPE.ADSB_EMITTER_TYPE_SMALL, R.string.adsb_t_small)
    ,   Large(ADSB_EMITTER_TYPE.ADSB_EMITTER_TYPE_LARGE, R.string.adsb_t_large)
    ,   HiVortexLarge(ADSB_EMITTER_TYPE.ADSB_EMITTER_TYPE_HIGH_VORTEX_LARGE, R.string.adsb_t_hv_large)
    ,   Heavy(ADSB_EMITTER_TYPE.ADSB_EMITTER_TYPE_HEAVY, R.string.adsb_t_heavy)
    ,   HighlyManuv(ADSB_EMITTER_TYPE.ADSB_EMITTER_TYPE_HIGHLY_MANUV, R.string.adsb_t_highly_manuv)
    ,   RotoCraft(ADSB_EMITTER_TYPE.ADSB_EMITTER_TYPE_ROTOCRAFT, R.string.adsb_t_rotocraft)
    ,   Unassigned(ADSB_EMITTER_TYPE.ADSB_EMITTER_TYPE_UNASSIGNED, R.string.adsb_t_unassigned)
    ,   Glider(ADSB_EMITTER_TYPE.ADSB_EMITTER_TYPE_GLIDER, R.string.adsb_t_glider)
    ,   LighterThanAir(ADSB_EMITTER_TYPE.ADSB_EMITTER_TYPE_LIGHTER_AIR, R.string.adsb_t_lighter_air)
    ,   Parachute(ADSB_EMITTER_TYPE.ADSB_EMITTER_TYPE_PARACHUTE, R.string.adsb_t_parachute)
    ,   Ultralight(ADSB_EMITTER_TYPE.ADSB_EMITTER_TYPE_ULTRA_LIGHT, R.string.adsb_t_ultralight)
    ,   Unassigned2(ADSB_EMITTER_TYPE.ADSB_EMITTER_TYPE_UNASSIGNED2, R.string.adsb_t_unassigned2)
    ,   UAV(ADSB_EMITTER_TYPE.ADSB_EMITTER_TYPE_UAV, R.string.adsb_t_uav)
    ,   Space(ADSB_EMITTER_TYPE.ADSB_EMITTER_TYPE_SPACE, R.string.adsb_t_space)
    ,   Unassigned3(ADSB_EMITTER_TYPE.ADSB_EMITTER_TYPE_UNASSGINED3, R.string.adsb_t_unassigned3)
    ,   EmergencySurface(ADSB_EMITTER_TYPE.ADSB_EMITTER_TYPE_EMERGENCY_SURFACE, R.string.adsb_t_emergency_surface)
    ,   ServiceSurface(ADSB_EMITTER_TYPE.ADSB_EMITTER_TYPE_SERVICE_SURFACE, R.string.adsb_t_service_surface)
    ,   PointObstacle(ADSB_EMITTER_TYPE.ADSB_EMITTER_TYPE_POINT_OBSTACLE, R.string.adsb_t_point_obstacle)
    ;

        private final int emitterType;
        private final int resId;

        Type(int type, int stringId) {
            emitterType = type;
            resId = stringId;
        }

        public int getEmitterType() { return emitterType; }
        public String getLabel(Context context) { return (resId == 0)? "Unknown": context.getString(resId); }

        static Type fromId(int id) {
            for(Type t: values()) {
                if(t.emitterType == id) {
                    return t;
                }
            }

            return Unknown;
        }
    }

    public static ADSBVehicle populate(ADSBVehicle v, msg_adsb_vehicle msg) {
        v.icaoAddress = msg.ICAO_address;

        double newLat = msg.lat / 1E7;
        double newLong = msg.lon / 1E7;
        double altMeters = (msg.altitude / 1000); // mm->m
        v.coord = new LatLongAlt(newLat, newLong, altMeters);
        v.heading = (msg.heading / 100);
        v.horizVelocity = msg.hor_velocity / 100; // cm/s -> m/s
        v.vertVelocity = msg.ver_velocity / 100; // cm/s -> m/s

        v.flags = msg.flags;
        v.squawk = msg.squawk;
        v.altitudeType = msg.altitude_type;
        v.callSign = msg.getCallsign();
        v.emitterType = msg.emitter_type;
        v.tslc = msg.tslc;
        v.type = Type.fromId(msg.emitter_type);

        return v;
    }

    private long icaoAddress;
    private LatLongAlt coord;
    private double heading;
    private double horizVelocity; // meters/second
    private double vertVelocity; // meters/second
    private int squawk;
    private int flags; // ADSB_FLAGS
    private int altitudeType; // ADSB_ALTITUDE_TYPE
    private int emitterType; // ADSB_EMITTER_TYPE
    private int tslc;
    private String callSign;
    private Type type = Type.Unknown;

    public ADSBVehicle() {
        super();
    }

    public long getIcaoAddress() {
        return icaoAddress;
    }

    public LatLongAlt getCoord() {
        return coord;
    }

    public double getHeading() {
        return heading;
    }

    public double getHorizVelocity() {
        return horizVelocity;
    }

    public double getVertVelocity() {
        return vertVelocity;
    }

    public int getSquawk() {
        return squawk;
    }

    public int getFlags() {
        return flags;
    }

    public int getAltitudeType() {
        return altitudeType;
    }

    public int getEmitterType() {
        return emitterType;
    }

    public int getTslc() {
        return tslc;
    }

    public String getCallSign() {
        return callSign;
    }

    public Type getType() { return type; }

    @Override
    public String toString() {
        return "ADSBVehicle{" +
                "icaoAddress=" + icaoAddress +
                ", coord=" + coord +
                ", heading=" + heading +
                ", horizVelocity=" + horizVelocity +
                ", vertVelocity=" + vertVelocity +
                ", squawk=" + squawk +
                ", flags=" + flags +
                ", altitudeType=" + altitudeType +
                ", emitterType=" + emitterType +
                ", tslc=" + tslc +
                ", type=" + type +
                ", callSign='" + callSign + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(icaoAddress);
        dest.writeParcelable(coord, 0);
        dest.writeDouble(heading);
        dest.writeDouble(horizVelocity);
        dest.writeDouble(vertVelocity);
        dest.writeInt(squawk);
        dest.writeInt(flags);
        dest.writeInt(altitudeType);
        dest.writeInt(emitterType);
        dest.writeInt(tslc);
        dest.writeString(callSign);
        dest.writeInt(type.ordinal());
    }

    private ADSBVehicle(Parcel in) {
        icaoAddress = in.readLong();
        coord = in.readParcelable(LatLongAlt.class.getClassLoader());
        heading = in.readDouble();
        horizVelocity = in.readDouble();
        vertVelocity = in.readDouble();
        squawk = in.readInt();
        flags = in.readInt();
        altitudeType = in.readInt();
        emitterType = in.readInt();
        tslc = in.readInt();
        callSign = in.readString();
        type = Type.fromId(in.readInt());
    }

    public static final Creator<ADSBVehicle> CREATOR = new Creator<ADSBVehicle>() {
        public ADSBVehicle createFromParcel(Parcel source) {
            return new ADSBVehicle(source);
        }

        public ADSBVehicle[] newArray(int size) {
            return new ADSBVehicle[size];
        }
    };
}

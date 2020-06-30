package com.o3dr.services.android.lib.drone.property;

import com.MAVLink.common.msg_distance_sensor;
import com.MAVLink.enums.MAV_DISTANCE_SENSOR;

public class DistanceSensor {
    public enum Type {
        Laser(MAV_DISTANCE_SENSOR.MAV_DISTANCE_SENSOR_LASER),
        Ultrasound(MAV_DISTANCE_SENSOR.MAV_DISTANCE_SENSOR_ULTRASOUND),
        Infrared(MAV_DISTANCE_SENSOR.MAV_DISTANCE_SENSOR_INFRARED),
        Radar(MAV_DISTANCE_SENSOR.MAV_DISTANCE_SENSOR_RADAR),
        Unknown(MAV_DISTANCE_SENSOR.MAV_DISTANCE_SENSOR_UNKNOWN)
        ;

        final int mavType;
        Type(int type) {
            this.mavType = type;
        }

        public int mavType() { return mavType; }

        public static Type fromType(int type) {
            for(Type t: values()) {
                if(t.mavType == type) {
                    return t;
                }
            }

            return Unknown;
        }
    }

    public enum Orientation {
        Forward(0, "Forward"),
        ForwardRight(1, "Forward Right"),
        Right(2, "Right"),
        BackRight(3, "Back Right"),
        Back(4, "Back"),
        BackLeft(5, "Back Left"),
        Left(6, "Left"),
        ForwardLeft(7, "Forward Left"),
        Up(24, "Up"),
        Down(25, "Down")
        ;

        private final int direction;
        private final String label;
        
        Orientation(int direction, String label) {
            this.direction = direction;
            this.label = label;
        }

        public int getDirection() { return direction; }
        public String getLabel() { return this.label; }

        public static Orientation from(int o) {
            for(Orientation v: values()) {
                if(v.direction == o) {
                    return v;
                }
            }

            return Forward;
        }
    }

    public static DistanceSensor populate(DistanceSensor sensor, msg_distance_sensor msg) {
        sensor.minDistance = msg.min_distance;
        sensor.maxDistance = msg.max_distance;
        sensor.currDistance = msg.current_distance;
        sensor.type = Type.fromType(msg.type);
        sensor.id = msg.id;
        sensor.orientation = Orientation.from(msg.orientation);
        sensor.covariance = msg.covariance;
        return sensor;
    }

    private int minDistance;
    private int maxDistance;
    private int currDistance;
    private Type type;
    private int id;
    private Orientation orientation;
    private short covariance;

    public DistanceSensor() {
        super();
    }

    public int getMinDistance() {
        return minDistance;
    }

    public int getMaxDistance() {
        return maxDistance;
    }

    public int getCurrDistance() {
        return currDistance;
    }

    public Type getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public short getCovariance() {
        return covariance;
    }

    @Override
    public String toString() {
        return "DistanceSensor{" +
                "minDistance=" + minDistance +
                ", maxDistance=" + maxDistance +
                ", currDistance=" + currDistance +
                ", type=" + type +
                ", id=" + id +
                ", orientation=" + orientation +
                ", covariance=" + covariance +
                '}';
    }
}

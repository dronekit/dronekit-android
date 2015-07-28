package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import android.util.Log;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVMessageTypes.*;

/**
 * Utility class to generate tlv packet from received bytes.
 */
public class TLVMessageParser {

    private static final String TAG = TLVMessageParser.class.getSimpleName();

    public static TLVPacket parseTLVPacket(byte[] packetData){
        if(packetData == null || packetData.length == 0)
            return null;

        return parseTLVPacket(ByteBuffer.wrap(packetData));
    }

    public static TLVPacket parseTLVPacket(ByteBuffer packetBuffer) {
        if (packetBuffer == null || packetBuffer.limit() <= 0)
            return null;

        final ByteOrder originalOrder = packetBuffer.order();

        int messageType = -1;
        try {
            packetBuffer.order(TLVPacket.TLV_BYTE_ORDER);

            messageType = packetBuffer.getInt();
            final int messageLength = packetBuffer.getInt();
            Log.d(TAG, String.format("Received message %d of with value of length %d", messageType, messageLength));

            switch (messageType) {
                case TYPE_SOLO_MESSAGE_GET_CURRENT_SHOT:
                case TYPE_SOLO_MESSAGE_SET_CURRENT_SHOT: {
                    final int shotType = packetBuffer.getInt();
                    if (messageType == TYPE_SOLO_MESSAGE_GET_CURRENT_SHOT)
                        return new SoloMessageShotGetter(shotType);
                    else
                        return new SoloMessageShotSetter(shotType);
                }

                case TYPE_SOLO_MESSAGE_LOCATION: {
                    final double latitude = packetBuffer.getDouble();
                    final double longitude = packetBuffer.getDouble();
                    final float altitude = packetBuffer.getFloat();
                    return new SoloMessageLocation(latitude, longitude, altitude);
                }

                case TYPE_SOLO_MESSAGE_RECORD_POSITION: {
                    return new SoloMessageRecordPosition();
                }

                case TYPE_SOLO_CABLE_CAM_OPTIONS: {
                    final short camInterpolation = packetBuffer.getShort();
                    final short yawDirectionClockwise = packetBuffer.getShort();
                    final float cruiseSpeed = packetBuffer.getFloat();
                    return new SoloCableCamOptions(camInterpolation, yawDirectionClockwise, cruiseSpeed);
                }

                case TYPE_SOLO_GET_BUTTON_SETTING:
                case TYPE_SOLO_SET_BUTTON_SETTING: {
                    final int button = packetBuffer.getInt();
                    final int event = packetBuffer.getInt();
                    final int shotType = packetBuffer.getInt();
                    final int flightMode = packetBuffer.getInt();
                    if (messageType == TYPE_SOLO_GET_BUTTON_SETTING)
                        return new SoloButtonSettingGetter(button, event, shotType, flightMode);
                    else
                        return new SoloButtonSettingSetter(button, event, shotType, flightMode);
                }

                case TYPE_SOLO_FOLLOW_OPTIONS:{
                    final float cruiseSpeed = packetBuffer.getFloat();
                    final int lookAtValue = packetBuffer.getInt();
                    return new SoloFollowOptions(cruiseSpeed, lookAtValue);
                }

                case TYPE_SOLO_SHOT_OPTIONS: {
                    final float cruiseSpeed = packetBuffer.getFloat();
                    return new SoloShotOptions(cruiseSpeed);
                }

                case TYPE_SOLO_SHOT_ERROR: {
                    final int errorType = packetBuffer.getInt();
                    return new SoloShotError(errorType);
                }

                case TYPE_SOLO_MESSAGE_SHOT_MANAGER_ERROR: {
                    final byte[] exceptionData = new byte[messageLength];
                    packetBuffer.get(exceptionData);
                    return new SoloMessageShotManagerError(new String(exceptionData));
                }

                case TYPE_SOLO_CABLE_CAM_WAYPOINT: {
                    final double latitude = packetBuffer.getDouble();
                    final double longitude = packetBuffer.getDouble();
                    final float altitude = packetBuffer.getFloat();
                    final float degreesYaw = packetBuffer.getFloat();
                    final float pitch = packetBuffer.getFloat();

                    return new SoloCableCamWaypoint(latitude, longitude, altitude, degreesYaw, pitch);
                }

                case TYPE_ARTOO_INPUT_REPORT_MESSAGE: {
                    final double timestamp = packetBuffer.getDouble();
                    final short gimbalY = packetBuffer.getShort();
                    final short gimbalRate = packetBuffer.getShort();
                    final short battery = packetBuffer.getShort();

                    return new ArtooMessageInputReport(timestamp, gimbalY, gimbalRate, battery);
                }

                default:
                    return null;
            }
        }catch(BufferUnderflowException e){
            Log.e(TAG, "Invalid data for tlv packet of type " + messageType);
            return null;
        } finally {
            packetBuffer.order(originalOrder);
        }
    }

    //Private constructor to prevent instantiation
    private TLVMessageParser() {}
}

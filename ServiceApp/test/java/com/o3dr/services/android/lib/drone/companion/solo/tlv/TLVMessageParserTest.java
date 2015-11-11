package com.o3dr.services.android.lib.drone.companion.solo.tlv;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit test for the TLVMessageParser class.
 */
public class TLVMessageParserTest {

    @Test
    public void testParseTLVPacket() throws Exception {
        List<TLVPacket> results = null;

        //Test for invalid parameters
        results = TLVMessageParser.parseTLVPacket((byte[]) null);
        assertNull(results);

        results = TLVMessageParser.parseTLVPacket(new byte[0]);
        assertNull(results);

        //Single message parsing test
        SoloMessageLocation messageLoc = new SoloMessageLocation(5.3488066,-4.0499032, 10);
        byte[] messageLocBytes = messageLoc.toBytes();
        results = TLVMessageParser.parseTLVPacket(messageLocBytes);
        assertNotNull(results);
        assertTrue(results.size() == 1);

        TLVPacket resultPacket = results.get(0);
        assertTrue(resultPacket.getMessageLength() == messageLoc.getMessageLength());
        assertTrue(resultPacket.getMessageType() == messageLoc.getMessageType());
        assertTrue(resultPacket instanceof SoloMessageLocation);

        SoloMessageLocation castedResult = (SoloMessageLocation) resultPacket;
        assertTrue(castedResult.getCoordinate().equals(messageLoc.getCoordinate()));


        //Multiple message parsing test
        //1.
        ByteBuffer inputData = ByteBuffer.allocate(46);
        SoloMessageShotGetter shotGetter = new SoloMessageShotGetter(SoloMessageShot.SHOT_CABLECAM);
        TLVPacket[] inputPackets = {messageLoc, shotGetter};
        for(TLVPacket packet: inputPackets){
            inputData.put(packet.toBytes());
        }
        inputData.rewind();

        results = TLVMessageParser.parseTLVPacket(inputData);
        assertNotNull(results);
        assertTrue(results.size() == inputPackets.length);
        assertTrue(inputData.remaining() == 6);

        for(int i = 0; i < inputPackets.length; i++){
            TLVPacket inputPacket = inputPackets[i];
            TLVPacket outputPacket = results.get(i);
            assertTrue(inputPacket.equals(outputPacket));
        }

        //2.
        inputData = ByteBuffer.allocate(40);
        for(TLVPacket packet: inputPackets){
            inputData.put(packet.toBytes());
        }
        inputData.rewind();

        results = TLVMessageParser.parseTLVPacket(inputData);
        assertNotNull(results);
        assertTrue(results.size() == inputPackets.length);
        assertTrue(inputData.remaining() == 0);

        for(int i = 0; i < inputPackets.length; i++){
            TLVPacket inputPacket = inputPackets[i];
            TLVPacket outputPacket = results.get(i);
            assertTrue(inputPacket.equals(outputPacket));
        }
    }
}
package org.mdt.dronedelivery.process;

import io.dronefleet.mavlink.MavlinkMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class MavlinkMessageHandler {

    private final Map<Integer, LinkedHashMap<String, TelemetryData>> telemetryDataMap = new ConcurrentHashMap<>();


    public void handleMessage(MavlinkMessage<?> message, int port, DatagramSocket socket, InetAddress senderAddress) {
        try {
            LinkedHashMap<String, TelemetryData> portData = telemetryDataMap.computeIfAbsent(port, k -> new LinkedHashMap<>());
            String key = "default"; // or message.getOriginSystemId(), etc.

            TelemetryData telemetryData = portData.computeIfAbsent(key, k -> new TelemetryData(port));
            telemetryData.setGcsIp(senderAddress.getHostAddress());
            telemetryData.setSystemId(message.getOriginSystemId());


        } catch (Exception e) {
            log.error("Error handling message on port {}: {}", port, e.getMessage(), e);
        }
    }

}

package org.mdt.dronedelivery.process;

import io.dronefleet.mavlink.MavlinkMessage;
import io.dronefleet.mavlink.ardupilotmega.Wind;
import io.dronefleet.mavlink.common.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.mdt.dronedelivery.send.TelemetryWebSocketService;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Component
@Slf4j
public class MavlinkMessageHandler {

    private final Map<Integer, TelemetryData> telemetryDataMap = new ConcurrentHashMap<>();
    private final TelemetryWebSocketService telemetryWebSocketService;
    private final TelemetryUpdateService telemetryUpdateService;

    public MavlinkMessageHandler(TelemetryWebSocketService telemetryWebSocketService,
                                 TelemetryUpdateService telemetryUpdateService) {
        this.telemetryWebSocketService = telemetryWebSocketService;
        this.telemetryUpdateService = telemetryUpdateService;
    }

    public void handleMessage(MavlinkMessage<?> message,
                              int port,
                              InetAddress senderAddress) {
        try {
            TelemetryData telemetryData = telemetryDataMap.computeIfAbsent(port, TelemetryData::new);
            telemetryData.setGcsIp(senderAddress.getHostAddress());
            telemetryData.setSystemId(message.getOriginSystemId());

            Object payload = message.getPayload();

            if (payload instanceof GlobalPositionInt pos) {
                telemetryUpdateService.applyGlobalPosition(telemetryData, pos, port);

            } else if (payload instanceof SysStatus sysStatus) {
                telemetryUpdateService.applySysStatus(telemetryData, sysStatus);

            } else if (payload instanceof VfrHud vfrHud) {
                telemetryUpdateService.applyVfrHud(telemetryData, vfrHud);

            } else if (payload instanceof MissionCount missionCount) {
                telemetryUpdateService.onMissionCount(port, missionCount);

            } else if (payload instanceof MissionItemInt missionItemInt) {
                telemetryUpdateService.onMissionItemInt(port, missionItemInt);

            } else if (payload instanceof Wind wind) {
                telemetryUpdateService.applyWind(telemetryData, wind);

            } else if (payload instanceof GpsRawInt gpsRawInt) {
                telemetryUpdateService.applyGpsRaw(telemetryData, gpsRawInt);

            } else if (payload instanceof Attitude attitude) {
                telemetryUpdateService.applyAttitude(telemetryData, attitude);

            } else if (payload instanceof ServoOutputRaw servo) {
                telemetryUpdateService.applyServoOutputs(telemetryData, servo);
            }

            // Push updated telemetry for all ports
            pushTelemetryWebSocketAllPorts();

        } catch (Exception e) {
            log.error("Error handling MAVLink message on port {}: {}", port, e.getMessage(), e);
        }
    }

    private void pushTelemetryWebSocketAllPorts() {
        try {
            Map<String, Map<String, Object>> telemetryDataAllPorts = new HashMap<>();
            for (Map.Entry<Integer, TelemetryData> entry : telemetryDataMap.entrySet()) {
                int port = entry.getKey();
                TelemetryData data = entry.getValue();

                telemetryDataAllPorts.put(
                        String.valueOf(port),
                        TelemetryPayloadMapper.toMap(
                                data,
                                telemetryUpdateService.getWaypoints(port),
                                telemetryUpdateService.getHomeLocation(port)
                        )
                );
            }

            telemetryWebSocketService.sendTelemetryData(
                    Map.of("telemetry_data", telemetryDataAllPorts)
            );

        } catch (Exception e) {
            log.error("Error sending telemetry via WebSocket", e);
        }
    }
}

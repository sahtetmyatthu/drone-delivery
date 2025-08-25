package org.mdt.dronedelivery.process;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class TelemetryPayloadMapper {

    private TelemetryPayloadMapper() {}

    static Map<String, Object> toMap(TelemetryData data,
                                     List<TelemetryData.Waypoint> waypoints,
                                     TelemetryData.HomeLocation home) {
        Map<String, Object> map = new HashMap<>();

        // Basic telemetry
        map.put("port", data.getPort());
        map.put("systemId", data.getSystemId());
        map.put("gcsIp", data.getGcsIp());

        // Position & motion
        map.put("latitude", data.getLat());
        map.put("longitude", data.getLon());
        map.put("altitude", data.getAlt());
        map.put("heading", data.getHeading());
        map.put("groundSpeed", data.getGroundSpeed());
        map.put("verticalSpeed", data.getVerticalSpeed());

        // Flight parameters
        map.put("airspeed", data.getAirspeed());
        map.put("windVelocity", data.getWindVel());
        map.put("gpsHdop", data.getGpsHdop());
        map.put("roll", data.getRoll());
        map.put("pitch", data.getPitch());
        map.put("yaw", data.getYaw());

        // Battery
        map.put("batteryVoltage", data.getBatteryVoltage());
        map.put("batteryCurrent", data.getBatteryCurrent());

        // Waypoints & home
        map.put("waypoints", waypoints == null ? List.of() : waypoints);
        if (home != null) {
            map.put("homeLocation", Map.of("lat", home.getLat(), "lon", home.getLon()));
        } else {
            map.put("homeLocation", Map.of("lat", 0.0, "lon", 0.0));
        }

        // Advanced telemetry
        map.put("flightStatus", data.getFlightStatus());
        map.put("timeInAir", data.getTimeInAir());
        map.put("autoTime", data.getAutoTime());
        map.put("throttleActive", data.isThrottleActive());
        map.put("throttleTimeInAir", data.getTotalThrottleTime());
        map.put("distTraveled", data.getDistTraveled());
        map.put("distToHome", data.getDistToHome());
        map.put("wpDist", data.getWpDist());
        map.put("targetHeading", data.getTargetHeading());
        map.put("previousHeading", data.getPreviousHeading());

        // Servo channels
        map.put("ch3percent", data.getCh3percent());
        map.put("ch3out", data.getCh3out());
        map.put("ch9out", data.getCh9out());
        map.put("ch10out", data.getCh10out());
        map.put("ch11out", data.getCh11out());
        map.put("ch12out", data.getCh12out());

        // Time estimates
        map.put("tot", data.getTot());
        map.put("toh", data.getToh());

        // Timestamp
        map.put("timestamp", data.getTimestamp());

        return map;
    }
}

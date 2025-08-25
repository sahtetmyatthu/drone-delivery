package org.mdt.dronedelivery.process;

import io.dronefleet.mavlink.ardupilotmega.Wind;
import io.dronefleet.mavlink.common.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class TelemetryUpdateService {

    private final Map<Integer, List<TelemetryData.Waypoint>> waypointsMap = new ConcurrentHashMap<>();
    private final Map<Integer, TelemetryData.HomeLocation> homeLocations = new ConcurrentHashMap<>();
    private final Map<Integer, double[]> lastKnownPosition = new ConcurrentHashMap<>();

    // ------------------- Global Position -------------------
    public void applyGlobalPosition(TelemetryData telemetryData, GlobalPositionInt position, int port) {
        double lat = position.lat() / 1e7;
        double lon = position.lon() / 1e7;
        double alt = position.relativeAlt() / 1000.0;

        telemetryData.setLat(lat);
        telemetryData.setLon(lon);
        telemetryData.setAlt(alt);
        telemetryData.setHeading(position.hdg() / 100f);
        telemetryData.setGroundSpeed(position.vx() / 100.0);
        telemetryData.setVerticalSpeed(position.vz() / 100.0);

        updateDistance(telemetryData, lat, lon);
        updateTimeInAir(telemetryData, alt);
    }

    private void updateDistance(TelemetryData telemetryData, double lat, double lon) {
        double[] lastPos = lastKnownPosition.get(telemetryData.getPort());
        if (lastPos != null) {
            double segment = calculateDistance(lastPos[0], lastPos[1], lat, lon) * 1000.0;
            telemetryData.setDistTraveled(telemetryData.getDistTraveled() + segment);
        }
        lastKnownPosition.put(telemetryData.getPort(), new double[]{lat, lon});

        TelemetryData.HomeLocation home = homeLocations.get(telemetryData.getPort());
        if (home != null) {
            double distToHome = calculateDistance(lat, lon, home.getLat(), home.getLon()) * 1000.0;
            telemetryData.setDistToHome(distToHome);
        }
    }

    private void updateTimeInAir(TelemetryData telemetryData, double alt) {
        long now = System.currentTimeMillis();
        if (alt > 0.5) {
            if (!telemetryData.isAirborne()) {
                telemetryData.setAirborne(true);
                telemetryData.setStartTime(now);
            }
            telemetryData.setTimeInAir((now - telemetryData.getStartTime()) / 1000);
        } else if (telemetryData.isAirborne()) {
            telemetryData.setTimeInAir((now - telemetryData.getStartTime()) / 1000);
            telemetryData.setAirborne(false);
        }
    }

    // ------------------- SysStatus -------------------
    public void applySysStatus(TelemetryData telemetryData, SysStatus sysStatus) {
        telemetryData.setBatteryVoltage(sysStatus.voltageBattery() / 1000.0);
        telemetryData.setBatteryCurrent(sysStatus.currentBattery() / 100.0);
    }

    // ------------------- VFR HUD -------------------
    public void applyVfrHud(TelemetryData telemetryData, VfrHud vfrHud) {
        telemetryData.setAirspeed(vfrHud.airspeed());
        telemetryData.setGroundSpeed(vfrHud.groundspeed());
        telemetryData.setVerticalSpeed(vfrHud.climb());
        telemetryData.setHeading(vfrHud.heading());

        // TOT & TOH calculation
        double wpDist = telemetryData.getWpDist();
        double gs = telemetryData.getGroundSpeed();
        telemetryData.setTot(gs > 0 ? Math.round((wpDist / gs) * 100.0) / 100.0 : 0.0);
        telemetryData.setToh(gs > 0 ? Math.round((telemetryData.getDistToHome() / gs) * 100.0) / 100.0 : 0.0);
    }

    // ------------------- Wind -------------------
    public void applyWind(TelemetryData telemetryData, Wind wind) {
        telemetryData.setWindVel(wind.speed());
    }

    // ------------------- GPS -------------------
    public void applyGpsRaw(TelemetryData telemetryData, GpsRawInt gpsRawInt) {
        telemetryData.setGpsHdop(gpsRawInt.eph());
    }

    // ------------------- Mission -------------------
    public void onMissionCount(int port, MissionCount missionCount) {
        waypointsMap.put(port, new ArrayList<>(Math.max(0, missionCount.count())));
        log.info("MISSION_COUNT on port {}: {}", port, missionCount.count());
    }

    public void onMissionItemInt(int port, MissionItemInt missionItemInt) {
        double lat = missionItemInt.x() / 1e7;
        double lon = missionItemInt.y() / 1e7;
        double alt = missionItemInt.z();

        if (lat == 0.0 && lon == 0.0 && alt == 0.0) return;

        TelemetryData.Waypoint wp = new TelemetryData.Waypoint();
        wp.setSeq(missionItemInt.seq());
        wp.setLat(lat);
        wp.setLon(lon);
        wp.setAlt(alt);

        waypointsMap.computeIfAbsent(port, k -> new ArrayList<>()).add(wp);

        if (missionItemInt.seq() == 0) {
            TelemetryData.HomeLocation home = new TelemetryData.HomeLocation();
            home.setLat(lat);
            home.setLon(lon);
            homeLocations.put(port, home);
        }
    }

    public List<TelemetryData.Waypoint> getWaypoints(int port) {
        return waypointsMap.getOrDefault(port, Collections.emptyList());
    }

    public TelemetryData.HomeLocation getHomeLocation(int port) {
        return homeLocations.get(port);
    }

    // ------------------- Attitude -------------------
    public void applyAttitude(TelemetryData telemetryData, Attitude attitude) {
        telemetryData.setRoll(Math.round(Math.toDegrees(attitude.roll()) * 100.0) / 100.0);
        telemetryData.setPitch(Math.round(Math.toDegrees(attitude.pitch()) * 100.0) / 100.0);
        telemetryData.setYaw(Math.round(Math.toDegrees(attitude.yaw()) * 100.0) / 100.0);
    }

    // ------------------- Servo / Flight Logic -------------------
    public void applyServoOutputs(TelemetryData telemetryData, ServoOutputRaw servoOutputRaw) {
        int ch3out = servoOutputRaw.servo3Raw();
        telemetryData.setCh3out(ch3out);
        telemetryData.setCh3percent(Math.round(((ch3out - 1000.0) / 1000.0) * 100 * 100.0) / 100.0);
        telemetryData.setCh9out(servoOutputRaw.servo9Raw());
        telemetryData.setCh10out(servoOutputRaw.servo10Raw());
        telemetryData.setCh11out(servoOutputRaw.servo11Raw());
        telemetryData.setCh12out(servoOutputRaw.servo12Raw());

        // Flight status and auto time
        telemetryData.setFlightStatus(ch3out > 1050 ? 1 : 0);
        telemetryData.setAutoTime(calculateAutoTime(telemetryData, ch3out));

        // Throttle logic
        boolean throttleActive = ch3out > 1050;
        telemetryData.setThrottleActive(throttleActive);
        telemetryData.setTotalThrottleTime(calculateThrottleTimeInAir(telemetryData, throttleActive));
    }

    private int calculateAutoTime(TelemetryData telemetryData, int ch3out) {
        long now = System.currentTimeMillis();
        if (ch3out > 1050) {
            if (!telemetryData.isFlying()) {
                telemetryData.setFlying(true);
                telemetryData.setFlightStartTime(now);
            }
            return (int) ((now - telemetryData.getFlightStartTime()) / 1000);
        } else {
            if (telemetryData.isFlying()) {
                telemetryData.setAutoTime((int) ((now - telemetryData.getFlightStartTime()) / 1000));
                telemetryData.setFlying(false);
            }
        }
        return telemetryData.getAutoTime();
    }

    private long calculateThrottleTimeInAir(TelemetryData telemetryData, boolean throttleActive) {
        long now = System.currentTimeMillis();
        if (throttleActive && !telemetryData.isThrottleActive()) {
            telemetryData.setThrottleActive(true);
            telemetryData.setThrottleStartTime(now);
        } else if (!throttleActive && telemetryData.isThrottleActive()) {
            telemetryData.setTotalThrottleTime(telemetryData.getTotalThrottleTime() + (now - telemetryData.getThrottleStartTime()));
            telemetryData.setThrottleActive(false);
        } else if (throttleActive) {
            telemetryData.setTotalThrottleTime(telemetryData.getTotalThrottleTime() + (now - telemetryData.getThrottleStartTime()));
            telemetryData.setThrottleStartTime(now);
        }
        return telemetryData.getTotalThrottleTime();
    }

    // ------------------- Utilities -------------------
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}

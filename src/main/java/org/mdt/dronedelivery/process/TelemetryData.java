package org.mdt.dronedelivery.process;

import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TelemetryData {

    private int port;
    private String gcsIp;
    private int systemId;
    private double lat;
    private double lon;
    private double alt;
    private double distTraveled;
    private double wpDist;
    private float heading;
    private float targetHeading;
    private float previousHeading;
    private double distToHome;
    private double verticalSpeed;
    private double groundSpeed;
    private double windVel;
    private double airspeed;
    private double gpsHdop;
    private double roll;
    private double pitch;
    private double yaw;
    private double ch3percent;
    private int ch3out;
    private int ch9out;
    private int ch10out;
    private int ch11out;
    private int ch12out;
    private double tot;
    private double toh;
    private long timeInAir;
    private double batteryVoltage;
    private double batteryCurrent;
    private int waypointsCount;
    private HomeLocation homeLocation;
    private List<Waypoint> waypoints;
    private boolean airborne;
    private long startTime;
    private long throttleStartTime;
    private long totalThrottleTime;
    private boolean throttleActive;
    private boolean flying;
    private long flightStartTime;
    private int autoTime;
    private int flightStatus;

    private String timestamp;


    public TelemetryData() {
        this.port = 0;
        this.gcsIp = "Unknown";
        this.systemId = 0;
        this.lat = 0.0;
        this.lon = 0.0;
        this.alt = 0.0;
        this.distTraveled = 0.0;
        this.wpDist = 0.0;
        this.heading = 0;
        this.targetHeading = 0;
        this.previousHeading = 0;
        this.distToHome = 0.0;
        this.verticalSpeed = 0.0;
        this.groundSpeed = 0.0;
        this.windVel = 0.0;
        this.airspeed = 0.0;
        this.gpsHdop = 0.0;
        this.roll = 0.0;
        this.pitch = 0.0;
        this.yaw = 0.0;
        this.ch3percent = 0.0;
        this.ch3out = 0;
        this.ch9out = 0;
        this.ch10out = 0;
        this.ch11out = 0;
        this.ch12out = 0;
        this.tot = 0.0;
        this.toh = 0.0;
        this.timeInAir = 0;
        this.batteryVoltage = 0.0;
        this.batteryCurrent = 0.0;
        this.waypointsCount = 0;
        this.homeLocation = new HomeLocation();
        this.waypoints = new ArrayList<>();
        this.airborne = false;
        this.startTime = 0L;
        this.throttleStartTime = 0L;
        this.totalThrottleTime = 0L;
        this.throttleActive = false;
        this.flying = false;
        this.flightStartTime = 0L;
        this.autoTime = 0;
        this.flightStatus = 0;
        this.timestamp = "";
    }

    // Constructor with port
    public TelemetryData(int port) {
        this();
        this.port = port;
    }

    @Getter
    @Setter
    public static class HomeLocation {
        private double lat;
        private double lon;

        public HomeLocation() {
            this.lat = 0.0;
            this.lon = 0.0;
        }
    }

    @Getter
    @Setter
    public static class Waypoint {
        private int seq;
        private double lat;
        private double lon;
        private double alt;

        public Waypoint() {
            this.seq = 0;
            this.lat = 0.0;
            this.lon = 0.0;
            this.alt = 0.0;
        }
    }
}

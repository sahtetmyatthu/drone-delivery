package org.mdt.dronedelivery.process;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class TelemetryData {
    // Constants for map keys to avoid hardcoded strings
    public static final String GCS_IP_KEY = "GCS_IP";
    public static final String UNKNOWN_GCS_IP = "Unknown";
    public static final String SYSTEM_ID_KEY = "system_id";
    public static final String FLIGHT_STATUS_KEY = "flight_status";
    public static final String AUTO_TIME_KEY = "auto_time";
    public static final String THROTTLE_ACTIVE_KEY = "throttle_active";
    public static final String LAT_KEY = "lat";
    public static final String LON_KEY = "lon";
    public static final String ALT_KEY = "alt";
    public static final String DIST_TRAVELED_KEY = "dist_traveled";
    public static final String WP_DIST_KEY = "wp_dist";
    public static final String HEADING_KEY = "heading";
    public static final String TARGET_HEADING_KEY = "target_heading";
    public static final String PREVIOUS_HEADING_KEY = "previous_heading";
    public static final String DIST_TO_HOME_KEY = "dist_to_home";
    public static final String VERTICAL_SPEED_KEY = "vertical_speed";
    public static final String GROUND_SPEED_KEY = "ground_speed";
    public static final String WIND_VEL_KEY = "wind_vel";
    public static final String AIRSPEED_KEY = "airspeed";
    public static final String GPS_HDOP_KEY = "gps_hdop";
    public static final String ROLL_KEY = "roll";
    public static final String PITCH_KEY = "pitch";
    public static final String YAW_KEY = "yaw";
    public static final String CH3PERCENT_KEY = "ch3percent";
    public static final String CH9OUT_KEY = "ch9out";
    public static final String TOT_KEY = "tot";
    public static final String TOH_KEY = "toh";
    public static final String TIME_IN_AIR_KEY = "time_in_air";
    public static final String THROTTLE_TIME_IN_AIR_KEY = "throttle_time_in_air";
    public static final String CH10OUT_KEY = "ch10out";
    public static final String CH11OUT_KEY = "ch11out";
    public static final String CH12OUT_KEY = "ch12out";
    public static final String BATTERY_VOLTAGE_KEY = "battery_voltage";
    public static final String BATTERY_CURRENT_KEY = "battery_current";
    public static final String WAYPOINTS_COUNT_KEY = "waypoints_count"; // Corrected typo from "waypoints_count—"

    private int port;
    private String gcsIp;
    private int systemId;
    private int flightStatus;
    private double autoTime;
    private boolean throttleActive;
    private double latitude;
    private double longitude;
    private double altitude;
    private double distTraveled;
    private int wpDist;
    private int heading;
    private int targetHeading;
    private int previousHeading;
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
    private double ch9out;
    private double tot;
    private double toh;
    private double timeInAir;
    private double throttleTimeInAir;
    private double ch10out;
    private double ch11out;
    private double ch12out;
    private int batteryVoltage;
    private double batteryCurrent;
    private int waypointsCount;


    public TelemetryData() {
        this.port = 0;
        this.gcsIp = UNKNOWN_GCS_IP;
        this.systemId = 0;
        this.flightStatus = 0;
        this.autoTime = 0.0;
        this.throttleActive = false;
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.altitude = 0.0;
        this.distTraveled = 0.0;
        this.wpDist = 0;
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
        this.ch9out = 0.0;
        this.tot = 0.0;
        this.toh = 0.0;
        this.timeInAir = 0.0;
        this.throttleTimeInAir = 0.0;
        this.ch10out = 0.0;
        this.ch11out = 0.0;
        this.ch12out = 0.0;
        this.batteryVoltage = 0;
        this.batteryCurrent = 0.0;
        this.waypointsCount = 0;
    }

    public TelemetryData(int port) {
        this();
        this.port = port;
    }
}
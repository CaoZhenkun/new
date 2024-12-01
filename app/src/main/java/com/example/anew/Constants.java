package com.example.anew;

import android.os.Build;

final public class Constants {

    public static final int VER_2_11 = 0;
    public static final int VER_3_03 = 1;
    public static final int Nav_Yes=1;
    public static final int Nav_No=0;

    public static final int STATUS_SATELLITE_GREEN = 30;
    public static final int STATUS_SATELLITE_YELLOW = 14;
    //public static final int STATUS_SATELLITE_RED = 2;


    public  static final  String KEY_NAV="rinexNav";
    public  static final  int DEF_RINEX_NAV=Constants.Nav_No;

    public static final String KEY_RINEX_VER = "rinexVer"; // value 0=2.11, 1=3.03
    public static final String KEY_MARK_NAME = "markName";
    public static final String KEY_MARK_TYPE = "markType";
    public static final String KEY_OBSERVER_NAME = "observerName";
    public static final String KEY_OBSERVER_AGENCY_NAME = "observerAgencyName";
    public static final String KEY_RECEIVER_NUMBER = "receiverNumber";
    public static final String KEY_RECEIVER_TYPE = "receiverType";
    public static final String KEY_RECEIVER_VERSION = "receiverVersion";
    public static final String KEY_ANTENNA_NUMBER = "antennaNumber";
    public static final String KEY_ANTENNA_TYPE = "antennaType";
    public static final String KEY_ANTENNA_ECCENTRICITY_EAST = "antennaEccentricityEast";
    public static final String KEY_ANTENNA_ECCENTRICITY_NORTH = "antennaEccentricityNorth";
    public static final String KEY_ANTENNA_HEIGHT = "antennaHeight";

    public static final int DEF_RINEX_VER = Constants.VER_3_03;
    public static final String DEF_MARK_NAME = "GnssRecord";
    public static final String DEF_MARK_TYPE = "Geodetic";
    public static final String DEF_OBSERVER_NAME = "RINEX Logger user";
    public static final String DEF_OBSERVER_AGENCY_NAME = "GnssRecord";
    public static final String DEF_RECEIVER_NUMBER = Build.SERIAL;
    public static final String DEF_RECEIVER_TYPE = Build.MANUFACTURER;
    public static final String DEF_RECEIVER_VERSION = Build.PRODUCT;
    public static final String DEF_ANTENNA_NUMBER = Build.SERIAL;
    public static final String DEF_ANTENNA_TYPE = Build.PRODUCT;
    public static final String DEF_ANTENNA_ECCENTRICITY_EAST = "0.0000";
    public static final String DEF_ANTENNA_ECCENTRICITY_NORTH = "0.0000";
    public static final String DEF_ANTENNA_HEIGHT = "0.0000";

    public static final String RINEX_SETTING = "gnss_record_setting";

    //广播星历下载方式
    public static final String KEY_DOWNLOAD_MODE="download mode";
    public static final int DEF_DOWNLOAD_MODE=-1;
    public static final int DOWNLOAD_MODE_NTRIP=0;
    public static final int DOWNLOAD_MODE_FTP=1;
    //挂载点设置
    public static final String KEY_MOUNTPOIT="mountpoint";
    public static final int DEF_MOUNTPOINT=-1;
    public static final int MOUNTPOINT_MIX=0;
    public static final int MOUNTPOINT_GPS=1;
    public static final int MOUNTPOINT_GAL=2;
    public static final int MOUNTPOINT_GLO=3;
    public static final int MOUNTPOINT_BDS=4;
    public static final int MOUNTPOINT_QZSS=5;


    /*
    spp setting
     */
    public static final String SPP_SETTING="spp setting";

    public static final String KEY_SPP_FILE="spp file";
    public static final int SPP_FILE=Constants.SPP_FILE_NO;
    public static final int SPP_FILE_YES=1;
    public static final int SPP_FILE_NO=0;


    public static final String KEY_SPP_MODEL="spp model";
    public static final int  DEF_SPP_MODEL=Constants.SPP_MODEL_SINGLE;

    public static final int SPP_MODEL_SINGLE=0;
    public static final int SPP_MODEL_DIFF=1;
    public static final int SPP_MODEL_ALL=2;

    public static final String KEY_doublefrequency="frequency";
    public static final int  DEF_frequency=0;

    public static final String KEY_Record="record";
    public static final int  DEF_Record=0;

    public static final String KEY_GPS_SYSTEM="gps system";
    public static final int  DEF_GPS_SYSTEM=Constants.GPS_NO;

    public static final String KEY_GAL_SYSTEM="gal system";
    public static final int  DEF_GAL_SYSTEM=Constants.GAL_NO;

    public static final String KEY_GLO_SYSTEM="glo system";
    public static final int  DEF_GLO_SYSTEM=Constants.GLO_NO;

    public static final String KEY_BDS_SYSTEM="bds system";
    public static final int  DEF_BDS_SYSTEM=Constants.BDS_NO;

    public static final String KEY_QZSS_SYSTEM="qzss system";
    public static final int  DEF_QZSS_SYSTEM=Constants.QZSS_NO;

    public static final int GPS_YES=1;
    public static final int GPS_NO=0;
    public static final int GAL_YES=1;
    public static final int GAL_NO=0;
    public static final int GLO_YES=1;
    public static final int GLO_NO=0;
    public static final int BDS_YES=1;
    public static final int BDS_NO=0;
    public static final int QZSS_YES=1;
    public static final int QZSS_NO=0;

    public static final String KEY_NTRIP_HOST="ntrip host";
    public static final String DEF_NTRIP_HOST="ntrip.gnsslab.cn";

    public static final String KEY_NTRIP_PORT="ntrip port";
    public static final String  DEF_NTRIP_PORT="2101";

    public static final String KEY_NTRIP_USERNAME="ntrip username";
    public static final String  DEF_NTRIP_USERNAME="tfbai";

    public static final String KEY_NTRIP_PASSWORD="ntrip password";
    public static final String  DEF_NTRIP_PASSWARD="tfbai@2020";

    // Speed of Light [m/s]
    public static final double SPEED_OF_LIGHT = 299792458.0;

    // Physical quantities as in IS-GPS
    public static final double EARTH_GRAVITATIONAL_CONSTANT = 3.986005e14;
    public static final double EARTH_ANGULAR_VELOCITY = 7.2921151467e-5;
    public static final double RELATIVISTIC_ERROR_CONSTANT = -4.442807633e-10;

    // GPS signal approximate travel time
    public static final double GPS_APPROX_TRAVEL_TIME = 0.072;

    // WGS84 ellipsoid features
    public static final double WGS84_SEMI_MAJOR_AXIS = 6378137;
    public static final double WGS84_FLATTENING = 1 / 298.257222101;
    public static final double WGS84_ECCENTRICITY = Math.sqrt(1 - Math.pow(
            (1 - WGS84_FLATTENING), 2));
    // Time-related values
    public static final double HUNDREDSMILLI = 1e-1; // 100 ms in seconds
    public static final double ONEMILLI = 1e-3;      // 1 ms in seconds

    public static final long DAYS_IN_WEEK = 7L;
    public static final long SEC_IN_DAY = 86400L;
    public static final long SEC_IN_HOUR = 3600L;
    public static final long MILLISEC_IN_SEC = 1000L;
    public static final long SEC_IN_HALF_WEEK = 302400L;
    // Days difference between UNIX time and GPS time
    public static final long UNIX_GPS_DAYS_DIFF = 3657L;
    public static final long UNIX_GST_DAYS_DIFF = 10825L; //935280000 seconds
    // MFB just add nanoseconds in a week
    public static final long NUMBER_NANO_SECONDS_PER_WEEK = 604800000000000L;
    public static final long WEEKSEC = 604800;

    public static final double NumberNanoSeconds100Milli = 1e8;         // 100 ms expressed in nanoseconds
    public static final double NumberNanoSeconds1Milli = 1e6;           // 100 ms expressed in nanoseconds

    // Standard atmosphere - Berg, 1948 (Bernese)
    public static final double STANDARD_PRESSURE = 1013.25;
    public static final double STANDARD_TEMPERATURE = 291.15;

    // Parameters to weigh observations by signal-to-noise ratio
    public static final float SNR_a = 30;
    public static final float SNR_A = 30;
    public static final float SNR_0 = 10;
    public static final float SNR_1 = 50;

    //GNSS frequencies
    public static final double FL1 = 1575.420e6; // GPS
    public static final double FL2 = 1227.600e6;
    public static final double FL5 = 1176.450e6;

    public static final double FR1_base = 1602.000e6; // GLONASS
    public static final double FR2_base = 1246.000e6;
    public static final double FR1_delta = 0.5625;
    public static final double FR2_delta = 0.4375;

    public static final double FE1  = FL1; // Galileo
    public static final double FE5a = FL5;
    public static final double FE5b = 1207.140e6;
    public static final double FE5  = 1191.795e6;
    public static final double FE6  = 1278.750e6;

    public static final double FC1  = 1589.740e6; // BeiDou
    public static final double FC2  = 1561.098e6;
    public static final double FC5b = FE5b;
    public static final double FC6  = 1268.520e6;

    public static final double FJ1  = FL1; // QZSS
    public static final double FJ2  = FL2;
    public static final double FJ5  = FL5;
    public static final double FJ6  = FE6;

    // other GNSS parameters
    public static final long ELL_A_GPS = 6378137;                          // GPS (WGS-84)     Ellipsoid semi-major axis [m]
    public static final long ELL_A_GLO = 6378136;                          // GLONASS (PZ-90)  Ellipsoid semi-major axis [m]
    public static final long ELL_A_GAL = 6378137;                          // Galileo (GTRF)   Ellipsoid semi-major axis [m]
    public static final long ELL_A_BDS = 6378136;                          // BeiDou (CSG2000) Ellipsoid semi-major axis [m]
    public static final long ELL_A_QZS = 6378137;                          // QZSS (WGS-84)    Ellipsoid semi-major axis [m]

    public static final double ELL_F_GPS = 1/298.257222101;                  // GPS (WGS-84)     Ellipsoid flattening
    public static final double ELL_F_GLO = 1/298.257222101;                  // GLONASS (PZ-90)  Ellipsoid flattening
    public static final double ELL_F_GAL = 1/298.257222101;                  // Galileo (GTRF)   Ellipsoid flattening
    public static final double ELL_F_BDS = 1/298.257222101;                  // BeiDou (CSG2000) Ellipsoid flattening
    public static final double ELL_F_QZS = 1/298.257222101;                  // QZSS (WGS-84)    Ellipsoid flattening

    public static final double ELL_E_GPS = Math.sqrt(1-(1- Math.pow(ELL_F_GPS, 2)));   // GPS (WGS-84)     Eccentricity
    public static final double ELL_E_GLO = Math.sqrt(1-(1- Math.pow(ELL_F_GLO, 2)));   // GLONASS (PZ-90)  Eccentricity
    public static final double ELL_E_GAL = Math.sqrt(1-(1- Math.pow(ELL_F_GAL, 2)));   // Galileo (GTRF)   Eccentricity
    public static final double ELL_E_BDS = Math.sqrt(1-(1- Math.pow(ELL_F_BDS, 2)));   // BeiDou (CSG2000) Eccentricity
    public static final double ELL_E_QZS = Math.sqrt(1-(1- Math.pow(ELL_F_QZS, 2)));   // QZSS (WGS-84)    Eccentricity

    public static final double GM_GPS = 3.986005e14;                     // GPS     Gravitational constant * (mass of Earth) [m^3/s^2]
    public static final double GM_GLO = 3.9860044e14;                    // GLONASS Gravitational constant * (mass of Earth) [m^3/s^2]
    public static final double GM_GAL = 3.986004418e14;                  // Galileo Gravitational constant * (mass of Earth) [m^3/s^2]
    public static final double GM_BDS = 3.986004418e14;                  // BeiDou  Gravitational constant * (mass of Earth) [m^3/s^2]
    public static final double GM_QZS = 3.986005e14;                     // QZSS    Gravitational constant * (mass of Earth) [m^3/s^2]

    public static final double OMEGAE_DOT_GPS = 7.2921151467e-5;             // GPS     Angular velocity of the Earth rotation [rad/s]
    public static final double OMEGAE_DOT_GLO = 7.292115e-5;                 // GLONASS Angular velocity of the Earth rotation [rad/s]
    public static final double OMEGAE_DOT_GAL = 7.2921151467e-5;             // Galileo Angular velocity of the Earth rotation [rad/s]
    public static final double OMEGAE_DOT_BDS = 7.292115e-5;                 // BeiDou  Angular velocity of the Earth rotation [rad/s]
    public static final double OMEGAE_DOT_QZS = 7.2921151467e-5;             // QZSS    Angular velocity of the Earth rotation [rad/s]

    public static final double J2_GLO = 1.0826257e-3;                        // GLONASS second zonal harmonic of the geopotential

    public static final double PI_ORBIT = 3.1415926535898;                   // pi value used for orbit computation
    public static final double CIRCLE_RAD = 2 * PI_ORBIT;                    // 2 pi

    public static final int CODE_NONE= 0 ;                  /* obs code: none or unknown */
    public static final int CODE_L1C= 1 ;                   /* obs code: L1C/A,G1C/A,E1C (GPS,GLO,GAL,QZS,SBS) */
    public static final int CODE_L1P= 2 ;                 /* obs code: L1P,G1P,B1P (GPS,GLO,BDS) */
    public static final int CODE_L1W= 3 ;                   /* obs code: L1 Z-track (GPS) */
    public static final int CODE_L1Y= 4 ;                   /* obs code: L1Y        (GPS) */
    public static final int CODE_L1M= 5 ;                   /* obs code: L1M        (GPS) */
    public static final int CODE_L1N= 6 ;                   /* obs code: L1codeless,B1codeless (GPS,BDS) */
    public static final int CODE_L1S= 7 ;                   /* obs code: L1C(D)     (GPS,QZS) */
    public static final int CODE_L1L= 8 ;                   /* obs code: L1C(P)     (GPS,QZS) */
    public static final int CODE_L1E= 9 ;                  /* (not used) */
    public static final int CODE_L1A= 10 ;                 /* obs code: E1A,B1A    (GAL,BDS) */
    public static final int CODE_L1B= 11 ;                 /* obs code: E1B        (GAL) */
    public static final int CODE_L1X= 12 ;                  /* obs code: E1B+C,L1C(D+P),B1D+P (GAL,QZS,BDS) */
    public static final int CODE_L1Z= 13 ;                  /* obs code: E1A+B+C,L1S (GAL,QZS) */
    public static final int CODE_L2C= 14 ;                  /* obs code: L2C/A,G1C/A (GPS,GLO) */
    public static final int CODE_L2D= 15 ;                 /* obs code: L2 L1C/A-(P2-P1) (GPS) */
    public static final int CODE_L2S = 16 ;                  /* obs code: L2C(M)     (GPS,QZS) */
    public static final int CODE_L2L= 17 ;                  /* obs code: L2C(L)     (GPS,QZS) */
    public static final int CODE_L2X= 18 ;                  /* obs code: L2C(M+L),B1_2I+Q (GPS,QZS,BDS) */
    public static final int CODE_L2P = 19 ;                  /* obs code: L2P,G2P    (GPS,GLO) */
    public static final int CODE_L2W= 20 ;                  /* obs code: L2 Z-track (GPS) */
    public static final int CODE_L2Y= 21 ;                  /* obs code: L2Y        (GPS) */
    public static final int CODE_L2M= 22 ;                  /* obs code: L2M        (GPS) */
    public static final int CODE_L2N= 23 ;                  /* obs code: L2codeless (GPS) */
    public static final int CODE_L5I= 24 ;                  /* obs code: L5I,E5aI   (GPS,GAL,QZS,SBS) */
    public static final int CODE_L5Q= 25 ;                  /* obs code: L5Q,E5aQ   (GPS,GAL,QZS,SBS) */
    public static final int CODE_L5X= 26 ;                  /* obs code: L5I+Q,E5aI+Q,L5B+C,B2aD+P (GPS,GAL,QZS,IRN,SBS,BDS) */
    public static final int CODE_L7I= 27 ;                  /* obs code: E5bI,B2bI  (GAL,BDS) */
    public static final int CODE_L7Q= 28 ;                  /* obs code: E5bQ,B2bQ  (GAL,BDS) */
    public static final int CODE_L7X= 29 ;                  /* obs code: E5bI+Q,B2bI+Q (GAL,BDS) */
    public static final int CODE_L6A= 30 ;                  /* obs code: E6A,B3A    (GAL,BDS) */
    public static final int CODE_L6B= 31 ;                  /* obs code: E6B        (GAL) */
    public static final int CODE_L6C= 32 ;                  /* obs code: E6C        (GAL) */
    public static final int CODE_L6X= 33 ;                  /* obs code: E6B+C,LEXS+L,B3I+Q (GAL,QZS,BDS) */
    public static final int CODE_L6Z= 34 ;                  /* obs code: E6A+B+C,L6D+E (GAL,QZS) */
    public static final int CODE_L6S= 35 ;                  /* obs code: L6S        (QZS) */
    public static final int CODE_L6L= 36 ;                  /* obs code: L6L        (QZS) */
    public static final int CODE_L8I= 37 ;                  /* obs code: E5abI      (GAL) */
    public static final int CODE_L8Q= 38 ;                  /* obs code: E5abQ      (GAL) */
    public static final int CODE_L8X= 39 ;                  /* obs code: E5abI+Q,B2abD+P (GAL,BDS) */
    public static final int CODE_L2I= 40 ;                  /* obs code: B1_2I      (BDS) */
    public static final int CODE_L2Q= 41 ;                  /* obs code: B1_2Q      (BDS) */
    public static final int CODE_L6I= 42 ;                  /* obs code: B3I        (BDS) */
    public static final int CODE_L6Q= 43 ;                  /* obs code: B3Q        (BDS) */
    public static final int CODE_L3I= 44 ;                  /* obs code: G3I        (GLO) */
    public static final int CODE_L3Q= 45 ;                  /* obs code: G3Q        (GLO) */
    public static final int CODE_L3X= 46 ;                  /* obs code: G3I+Q      (GLO) */
    public static final int CODE_L1I= 47 ;                  /* obs code: B1I        (BDS) (obsolute) */
    public static final int CODE_L1Q= 48 ;                  /* obs code: B1Q        (BDS) (obsolute) */
    public static final int CODE_L5A= 49 ;                  /* obs code: L5A SPS    (IRN) */
    public static final int CODE_L5B= 50 ;                  /* obs code: L5B RS(D)  (IRN) */
    public static final int CODE_L5C= 51 ;                  /* obs code: L5C RS(P)  (IRN) */
    public static final int CODE_L9A= 52 ;                  /* obs code: SA SPS     (IRN) */
    public static final int CODE_L9B= 53 ;                  /* obs code: SB RS(D)   (IRN) */
    public static final int CODE_L9C= 54 ;                  /* obs code: SC RS(P)   (IRN) */
    public static final int CODE_L9X= 55 ;                  /* obs code: SB+C       (IRN) */
    public static final int CODE_L1D= 56 ;                  /* obs code: B1D        (BDS) */
    public static final int CODE_L5D= 57 ;                  /* obs code: L5D(L5S),B2aD (QZS,BDS) */
    public static final int CODE_L5P= 58 ;                  /* obs code: L5P(L5S),B2aP (QZS,BDS) */
    public static final int CODE_L5Z= 59 ;                  /* obs code: L5D+P(L5S) (QZS) */
    public static final int CODE_L6E= 60 ;                  /* obs code: L6E        (QZS) */
    public static final int CODE_L7D= 61 ;                  /* obs code: B2bD       (BDS) */
    public static final int CODE_L7P= 62 ;                  /* obs code: B2bP       (BDS) */
    public static final int CODE_L7Z= 63 ;                  /* obs code: B2bD+P     (BDS) */
    public static final int CODE_L8D= 64 ;                  /* obs code: B2abD      (BDS) */
    public static final int CODE_L8P= 65 ;                  /* obs code: B2abP      (BDS) */
    public static final int CODE_L4A= 66 ;                  /* obs code: G1aL1OCd   (GLO) */
    public static final int CODE_L4B= 67 ;                  /* obs code: G1aL1OCd   (GLO) */
    public static final int CODE_L4X= 68 ;                  /* obs code: G1al1OCd+p (GLO) */
    public static final int MAXCODE = 68;                                    //obs码的最大数量
    public static final double MAXAGESSR_HRCLK=10.0;        //hrclk最大上限
    public static final double OMGE=7.2921151467E-5;        /* earth angular velocity (IS-GPS) (rad/s) */
    public static final double VAR_POS=60*60;               //位置协方差










}

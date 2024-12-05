package com.example.anew.Ntrip;

public class ConstantSystem {


    public  static final char GPS_SYSTEM='G';
    public  static final char GLONASS_SYSTEM='R';
    public static final char QZSS_SYSTEM='J';
    public  static final char BEIDOU_SYSTEM='C';
    public  static final char GALILEO_SYSTEM='E';
    public  static final char SBAS_SYSTEM='S';

    public static final int MAXSATIDGPS=32;
    public static final int MINSATIDGPS=1;

    public static final int MAXSATIDQZSS=199;
    public static final int MINSATIDQZSS=193;

    public static final int MAXSATIDGLO=24;
    public static final int MINSATIDGLO=1;

    public static final int MAXSATIDGAL=30;
    public static final int MINSATIDGAL=1;
    //先定义35颗
    public static final int MAXSATIDBDS=35;
    public static final int MINSATIDBDS=1;

    public static final int MINPRNGPS=1;
    public static final int MAXPRNGPS=32;
    public static final int NSATGPS=(MAXPRNGPS-MINPRNGPS+1);
    public static final int MINPRNGLO=1;
    public static final int MAXPRNGLO=27;
    public static final int NSATGLO=(MAXPRNGLO-MINPRNGLO+1);
    public static final int MINPRNGAL=1;
    public static final int MAXPRNGAL=36;
    public static final int NSATGAL=(MAXPRNGAL-MINPRNGAL+1);
    public static final int MINPRNQZS=193;
    public static final int MAXPRNQZS=202;
    public static final int NSATQZS=(MAXPRNQZS-MINPRNQZS+1);
    public static final int MINPRNCMP=1;
    public static final int MAXPRNCMP=63;
    public static final int NSATCMP=(MAXPRNCMP-MINPRNCMP+1);
    public static final int MAXSAT=(NSATGPS+NSATGLO+NSATGAL+NSATQZS+NSATCMP);

    //卫星系统
    public static final int SYS_NONE=0;
    public static final int SYS_GPS=1;
    public static final int SYS_GLO=2;
    public static final int SYS_GAL=3;
    public static final int SYS_BEIDOU=4;
    public static final int SYS_QZSS=5;
    public static final int SYS_SBAS=6;
    public static final int SYS_ALL=7;


    //在写程序的过程中发现  ftp获取的gps广播星历中的sqrt(A)与ntrip获取的gps广播星历中的sqrt(A)相减为一个定值8192

    public static final double UNKNOWN=8192.0;


}

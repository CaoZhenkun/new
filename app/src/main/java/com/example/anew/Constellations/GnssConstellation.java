package com.example.anew.Constellations;

import android.annotation.SuppressLint;
import android.location.GnssClock;

import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.location.GnssStatus;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import com.example.anew.Constants;
import com.example.anew.GNSSData;

import com.example.anew.PositioningData;

import com.example.anew.coord.Coordinates;
import com.example.anew.Satellites.*;
import com.example.anew.GNSSData;

import com.example.anew.coord.SatellitePosition;
import com.example.anew.corrections.*;
import com.example.anew.GNSSConstants;
import com.example.anew.navifromftp.RinexNavigationGps;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;

public class GnssConstellation   {


    private PositioningData positioningData;
    private static double L1_FREQUENCY = 1.57542e9;
    private static double L5_FREQUENCY = 1.17645e9;
    private static double B1I_FREQUENCY = 1.561098e9;
    private static double B1C_FREQUENCY = 1.575420e9;
    private static double B2a_FREQUENCY = 1.176450e9;
    private static final double E1a_FREQUENCY = 1.57542e9;
    private static double E5a_FREQUENCY = 1.17645e9;
    private static double FREQUENCY_MATCH_RANGE = 0.1e9;
    private int leapseconds = 18;
    private static double MASK_ELEVATION = 20; // degrees

    private boolean fullBiasNanosInitialized = false;
    private long FullBiasNanos;
    private double BiasNanos;
    protected double tRxGPS;
    protected double weekNumberNanos;
    private Handler uiHandler;
    public List<GNSSData> testList=new ArrayList<>();
    public List<GNSSData> testList2=new ArrayList<>();
    public static boolean approximateEqual(double a, double b, double eps) {
        return Math.abs(a - b) < eps;
    }


    /**
     * 以下参数是判断参与伪距单点/差分定位的系统，1表示参与，0表示未参与
     */
    private int GPS_SYSTEM;
    private int GAL_SYSTEM;
    private int GLO_SYSTEM;
    private int BDS_SYSTEM;
    private int QZSS_SYSTEM;


    private RinexNavigationGps mrinexNavigationMnew=new RinexNavigationGps();
    //private RinexNavigationParser mRinexNavigationParser=new RinexNavigationParser();


    public final static String NASA_NAVIGATION_HOURLY = "ftp://igs.gnsswhu.cn/pub/gps/data/hourly/${yyyy}/${ddd}/${hh4}/GANP00SVK_R_${yyyy}${ddd}${hh4}00_01H_MN.rnx.gz";
    public final static String WHU_GPS_HOURLY = "ftp://igs.gnsswhu.cn/pub/gps/data/hourly/${yyyy}/${ddd}/hour${ddd}0.${yy}n.gz";
    public final static String BKG_NAVIGATION ="ftp://igs-ftp.bkg.bund.de/IGS/BRDC/${yyyy}/${ddd}/BRDC00WRD_R_${yyyy}${ddd}0000_01D_MN.rnx.gz";
    public GnssConstellation(Handler uiHandler, int GPS_SYSTEM, int GAL_SYSTEM, int GLO_SYSTEM, int BDS_SYSTEM, int QZSS_SYSTEM, PositioningData positioningData) {
        this.uiHandler=uiHandler;
        this.GPS_SYSTEM = GPS_SYSTEM;
        this.GAL_SYSTEM = GAL_SYSTEM;
        this.GLO_SYSTEM = GLO_SYSTEM;
        this.BDS_SYSTEM = BDS_SYSTEM;
        this.QZSS_SYSTEM=QZSS_SYSTEM;
        this.positioningData=positioningData;

    }

//    public void onDataReceived(byte[] data) {//接收到NTRIP协议的数据后对数据进行解码
//        synchronized (this) {
//            int type = (int) getbitu(data, 0, 12);
//            Log.d("RTCM文件类型", "-" + type);
//            switch (type) {
//                case 1019://GPS
//                    decodeGpsEph(data);
//                case 1264://电离层
//                    decodeionodata(data);
//                case 1057://SSR改正
//                    decodeSSR1(data);
//                case 1058://SSR改正
//                    decodeSSR2(data);
//                case 1059://SSR改正
//                    decodeSSR3(data);
//            }
//        }
//    }
public void updateMeasurements(GnssMeasurementsEvent event) {

    synchronized (this) {
        //******************************************下面中文注释是AI生成，不可都信***************************************//
        //初始化变量
        testList.clear();
        //获取 GNSS 时钟信息
        GnssClock gnssClock = event.getClock();//获取 GNSS 时钟信息Gets the GNSS receiver clock information associated with the measurements for the current event.
        long TimeNanos = gnssClock.getTimeNanos();//获取当前时间的纳秒数
        //timeRefMsec = new Time(System.currentTimeMillis());//获取当前时间的毫秒数
        double BiasNanos = gnssClock.getBiasNanos();//获取时钟偏差的纳秒数
        double gpsTime, pseudorange;

        // Use only the first instance of the FullBiasNanos (as done in gps-measurement-tools)
        //仅使用 FullBiasNanos 的第一个实例（就像在 gps-measurement-tools 中所做的那样）
        if (!fullBiasNanosInitialized) {
            if(!gnssClock.hasFullBiasNanos())
            {
                return;
            }
            FullBiasNanos = gnssClock.getFullBiasNanos();
            fullBiasNanosInitialized = true;
        }



        for (GnssMeasurement measurement : event.getMeasurements()) {

            if (measurement.getConstellationType() != GnssStatus.CONSTELLATION_GPS)
                continue;

            if (measurement.hasCarrierFrequencyHz())
                if (!approximateEqual(measurement.getCarrierFrequencyHz(), L1_FREQUENCY, FREQUENCY_MATCH_RANGE))
                    continue;

            long ReceivedSvTimeNanos = measurement.getReceivedSvTimeNanos();
            double TimeOffsetNanos = measurement.getTimeOffsetNanos();

            gpsTime =
                    TimeNanos - (FullBiasNanos + BiasNanos); // TODO intersystem bias?


            tRxGPS =
                    gpsTime + TimeOffsetNanos;

            weekNumberNanos =
                    Math.floor((-1. * FullBiasNanos) / Constants.NUMBER_NANO_SECONDS_PER_WEEK)
                            * Constants.NUMBER_NANO_SECONDS_PER_WEEK;

            //计算伪距
            pseudorange =
                    (tRxGPS - weekNumberNanos - ReceivedSvTimeNanos) / 1.0E9
                            * Constants.SPEED_OF_LIGHT;

            // TODO Check that the measurement have a valid state such that valid pseudoranges are used in the PVT algorithm

                /*

                According to https://developer.android.com/ the GnssMeasurements States required
                for GPS valid pseudoranges are:

                int STATE_CODE_LOCK         = 1      (1 << 0)
                int int STATE_TOW_DECODED   = 8      (1 << 3)

                */
            //检查测量状态，确保测量状态满足条件（如 STATE_CODE_LOCK 和 STATE_TOW_DECODED）
            int measState = measurement.getState();

            // Bitwise AND to identify the states
            boolean codeLock = (measState & GnssMeasurement.STATE_CODE_LOCK) != 0;//检查是否已经码锁定（STATE_CODE_LOCK）
            boolean towDecoded = (measState & GnssMeasurement.STATE_TOW_DECODED) != 0;// 检查是否已经解码时间（STATE_TOW_DECODED）
            boolean towKnown = false;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                // 检查当前设备的 API 级别是否大于或等于 Android O（API 级别 26）
                towKnown = (measState & GnssMeasurement.STATE_TOW_KNOWN) != 0;
                // 如果设备的 API 级别大于或等于 Android O，检查是否已知时间（STATE_TOW_KNOWN）
                //STATE_TOW_KNOWN:这个GNSS测量的跟踪状态已经知道了周时间（Time-of-Week），可能没有通过空中解码，但已经通过其他来源确定。
            }
            //如果测量状态满足条件，则创建 SatelliteParameters 对象并添加到 observedSatellites 列表中。
            //否则，创建 SatelliteParameters 对象并添加到 unusedSatellites 列表中，并增加 visibleButNotUsed 计数。
            if (codeLock && (towDecoded || towKnown) && pseudorange < 1e9) { // && towUncertainty
                //存储卫星参数的对象

                GNSSData gnssData=new GNSSData();
                gnssData.setSATID(measurement.getSvid());
                gnssData.setpseudorange(pseudorange);
                gnssData.setGnssType('G');
                testList.add(gnssData);

            } else {

            }
        }
    }
}
//    public void updateMeasurements(GnssMeasurementsEvent event) {
//        synchronized (this) {
////            positioningData.gnssDataArrayList.clear();
////            positioningData.gpsDataList.clear();
////            positioningData.galileoDataList.clear();
////            positioningData.bdsDataList.clear();
////            positioningData.glonassDataList.clear();
////            positioningData.qzssDataList.clear();
////            positioningData.gnssDataArrayListtest.clear();
//            testList.clear();
//
//
//            GnssClock gnssClock = event.getClock();
//            positioningData.setEpochTime(new GpsTime(gnssClock));//历元的GPS时间
//            //使用当前历元的TimeNanos
//            long TimeNanos = gnssClock.getTimeNanos();
//            // 使用第一个历元的FullBiasNanos和BiasNanos
//            if (!fullBiasNanosInitialized) {
//                if(!gnssClock.hasFullBiasNanos())
//                {
//                    return;
//                }
//                FullBiasNanos = gnssClock.getFullBiasNanos();
//                if(gnssClock.hasBiasNanos())
//                {
//                    BiasNanos=gnssClock.getBiasNanos();
//                }
//                fullBiasNanosInitialized = true;
//            }
//
//            for (GnssMeasurement measurement : event.getMeasurements()) {
//                if(measurement.getConstellationType() != GnssStatus.CONSTELLATION_GPS)
//                {
//                    continue;
//                }
//
//                weekNumberNanos =
//                         (Math.floor((-1. * FullBiasNanos) / Constants.NUMBER_NANO_SECONDS_PER_WEEK)
//                                                        * Constants.NUMBER_NANO_SECONDS_PER_WEEK);
//
//                double TimeOffsetNanos=measurement.getTimeOffsetNanos();
//                double tRxNanos = TimeNanos+ TimeOffsetNanos - (FullBiasNanos + BiasNanos);//手机硬件时间减去与GPS时间的偏差，加偏差修正量,得到GPS时间
//                tRxGPS=tRxNanos;//GPS时间
//                tRxNanos=tRxNanos-weekNumberNanos;//GPS周内秒
//
//
//
//                if((measurement.getConstellationType() == GnssStatus.CONSTELLATION_GALILEO && measurement.getState()==GnssMeasurement.STATE_GAL_E1C_2ND_CODE_LOCK)){
//                    tRxNanos=tRxGPS% Constants.NumberNanoSeconds100Milli;//Galileo日内秒 for Galileo with E1C 2nd code status
//                }
//
//                double tRxSeconds = tRxNanos * 1e-9;
//                double tTxSeconds = measurement.getReceivedSvTimeNanos() * 1e-9;
//
//
//                if ((measurement.getConstellationType() == GnssStatus.CONSTELLATION_GLONASS)) {
//                    double tRxSeconds_GLO = tRxSeconds % 86400;
//                    double tTxSeconds_GLO = tTxSeconds - 10800 + leapseconds;
//                    if (tTxSeconds_GLO < 0) {
//                        tTxSeconds_GLO = tTxSeconds_GLO + 86400;
//                    }
//                    tRxSeconds = tRxSeconds_GLO;
//                    tTxSeconds = tTxSeconds_GLO;
//
//                }
//                if (measurement.getConstellationType() == GnssStatus.CONSTELLATION_BEIDOU) {
//                    double tRxSeconds_BDS = tRxSeconds;
//                    double tTxSeconds_BDS = tTxSeconds +14;
//                    if (tTxSeconds_BDS > 604800) {
//                        tTxSeconds_BDS = tTxSeconds_BDS - 604800;
//                    }
//
//                    tRxSeconds = tRxSeconds_BDS;
//                    tTxSeconds = tTxSeconds_BDS;
//
//                }
//
//                double prSeconds = tRxSeconds - tTxSeconds;
//                boolean iRollover = prSeconds > 604800 / 2;
//                if (iRollover) {
//                    double delS = Math.round(prSeconds / 604800) * 604800;
//                    double prS = prSeconds - delS;
//                    double maxBiasSeconds = 10;
//                    if (prS > maxBiasSeconds) {
//                        Log.e("RollOver", "Rollover Error");
//                        iRollover = true;
//                    } else {
//                        tRxSeconds = tRxSeconds - delS;
//                        prSeconds = tRxSeconds - tTxSeconds;
//                        iRollover = false;
//                    }
//                }
//                double pseudorange = prSeconds * 2.99792458e8;//伪距
//
//
//                double frequency=0;
//                String frequencyLable="";
//                char GnssType;
//                if(measurement.getConstellationType() == GnssStatus.CONSTELLATION_GPS)
//                {
//                    GnssType='G';
//                    if (approximateEqual(measurement.getCarrierFrequencyHz(), L1_FREQUENCY, FREQUENCY_MATCH_RANGE)) {
//                        frequency=L1_FREQUENCY;
//                        frequencyLable="L1";
//                    }
//                    else if (approximateEqual(measurement.getCarrierFrequencyHz(), L5_FREQUENCY, FREQUENCY_MATCH_RANGE)) {
//                        frequency = L5_FREQUENCY;
//                        frequencyLable = "L5";
//                    }
//                    else {
//                        continue;
//                    }
//                } else if (measurement.getConstellationType() == GnssStatus.CONSTELLATION_GALILEO) {
//                    GnssType='E';
//                    if (approximateEqual(measurement.getCarrierFrequencyHz(), L1_FREQUENCY, FREQUENCY_MATCH_RANGE)) {
//                        frequency=L1_FREQUENCY;
//                        frequencyLable="L1";
//                    }
//                    else if (approximateEqual(measurement.getCarrierFrequencyHz(), L5_FREQUENCY, FREQUENCY_MATCH_RANGE)) {
//                        frequency = L5_FREQUENCY;
//                        frequencyLable = "L5";
//                    }
//                    else {
//                        continue;
//                    }
//                }else if(measurement.getConstellationType() == GnssStatus.CONSTELLATION_GLONASS){
//                    GnssType='R';
//                    frequency=measurement.getCarrierFrequencyHz();
//                    frequencyLable="G1";
//                } else if (measurement.getConstellationType() == GnssStatus.CONSTELLATION_BEIDOU) {
//                    GnssType='C';
//                    if (approximateEqual(measurement.getCarrierFrequencyHz(), B1I_FREQUENCY, FREQUENCY_MATCH_RANGE)) {
//                        frequency=B1I_FREQUENCY;
//                        frequencyLable="B1";
//                    }
//                    else if (approximateEqual(measurement.getCarrierFrequencyHz(), B1C_FREQUENCY, FREQUENCY_MATCH_RANGE)) {
//                        frequency=B1C_FREQUENCY;
//                        frequencyLable="L1";
//                    }
//                    else if (approximateEqual(measurement.getCarrierFrequencyHz(), B2a_FREQUENCY, FREQUENCY_MATCH_RANGE)) {
//                        frequency=B2a_FREQUENCY;
//                        frequencyLable="L5";
//                    }
//                    else {
//                        continue;
//                    }
//                } else if (measurement.getConstellationType() == GnssStatus.CONSTELLATION_QZSS) {
//                    GnssType='J';
//                    if (approximateEqual(measurement.getCarrierFrequencyHz(), L1_FREQUENCY, FREQUENCY_MATCH_RANGE)) {
//                        frequency=L1_FREQUENCY;
//                        frequencyLable="L1";
//                    }
//                    else if (approximateEqual(measurement.getCarrierFrequencyHz(), L5_FREQUENCY, FREQUENCY_MATCH_RANGE)) {
//                        frequency = L5_FREQUENCY;
//                        frequencyLable = "L5";
//                    }
//                    else {
//                        continue;
//                    }
//                }else {
//                    continue;
//                }
//                if(frequency==0)
//                {
//                    frequency=measurement.getCarrierFrequencyHz();
//                }
//
//                int measState = measurement.getState();
//                // 观测值状态检测
//                boolean codeLock = (measState & GnssMeasurement.STATE_CODE_LOCK) != 0;
//                boolean towDecoded = (measState & GnssMeasurement.STATE_TOW_DECODED) != 0;
//                boolean towKnown = false;
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    towKnown = (measState & GnssMeasurement.STATE_TOW_KNOWN) != 0;
//                }
//                if (codeLock && (towDecoded || towKnown) && pseudorange < 1e9) {
//                    GNSSData gnssData=new GNSSData();
//                    gnssData.setSATstate(1);
//                    gnssData.setGnssType(GnssType);
//                    gnssData.setFrequency(frequency);
//                    gnssData.setFrequencyLable(frequencyLable);
//                    gnssData.setPrn(addprn(GnssType, measurement.getSvid()));//存储Prn与频率
//                    gnssData.setPrnAndF(addprn(GnssType,measurement.getSvid(),frequencyLable));
//                    gnssData.setpseudorange(pseudorange);//存储伪距
//                    gnssData.setSATID(measurement.getSvid());//存储卫星ID
//                    //载波观测值获取
//                    double ADR = measurement.getAccumulatedDeltaRangeMeters();
//                    double λ = 2.99792458e8 / frequency;
//                    double phase = ADR / λ;
//                    gnssData.setphase(phase);//存储载波
//
//                    gnssData.setSnr(measurement.getCn0DbHz());//存储获取载噪比
//
//                    gnssData.setDoppler(measurement.getPseudorangeRateMetersPerSecond() / λ);//存储多普勒值
//                    //positioningData.gnssDatahash.put(positioningData.gnssData.getPrn(),positioningData.gnssData);//将gnssdata存入哈希表中
//                    positioningData.gnssDataArrayList.add(gnssData);
//
//                    if(measurement.getConstellationType() == GnssStatus.CONSTELLATION_GPS)
//                    {
////                        positioningData.gpsDataList.add(gnssData);
////                        positioningData.gnssDataArrayListtest.add(gnssData);
//                        testList.add(gnssData);
//                    }
//                    else if(measurement.getConstellationType() == GnssStatus.CONSTELLATION_GALILEO)
//                    {
//                        positioningData.galileoDataList.add(gnssData);
//                    }
//                    else if(measurement.getConstellationType() == GnssStatus.CONSTELLATION_BEIDOU)
//                    {
//                        positioningData.bdsDataList.add(gnssData);
//                    }
//                    else if(measurement.getConstellationType() == GnssStatus.CONSTELLATION_GLONASS)
//                    {
//                        positioningData.glonassDataList.add(gnssData);
//                    }
//                    else if(measurement.getConstellationType() == GnssStatus.CONSTELLATION_QZSS)
//                    {
//                        positioningData.qzssDataList.add(gnssData);
//                    }
//
//
////                    sendUiUpdateMessage("调试", "伪距="+positioningData.gnssData.getpseudorange()+"卫星ID="+positioningData.gnssData.getPrn()+"\n"+"载波="+
////                            positioningData.gnssData.getphase()+"卫星状态="+ positioningData.gnssData.getSATstate());
//
//                }
//            }
//            //distributeData();
//            System.out.println("hhgg");
//        }
//    }

    public void distributeData() {
        // 清空
        positioningData.gpsSatelliteList.clear();
        positioningData.glonassSatelliteList.clear();
        positioningData.galileoSatelliteList.clear();
        positioningData.bdsSatelliteList.clear();
        positioningData.qzssSatelliteList.clear();
        positioningData.doubleFreeDatahash.clear();
        try {
            /**
             * 存放历元下的不同卫星的原始数据
             */
            List<GNSSData> epochgps_observedSatellites = positioningData.gpsDataList;
            List<GNSSData> epochqzss_observedSatellites = positioningData.qzssDataList;
            List<GNSSData> epochbds_observedSatellites = positioningData.bdsDataList;
            List<GNSSData> epochgalileo_observedSatellites = positioningData.galileoDataList;
            List<GNSSData> epochglonass_observedSatellites = positioningData.glonassDataList;
            List<String> hasDoublesvid = new ArrayList<>();

            {
                /**
                 * 处理gps卫星
                 */
                if (epochgps_observedSatellites.size() == 1) {
                    GpsSatellite gpsSatellite = new GpsSatellite();
                    int i = 0;
                    String prn = epochgps_observedSatellites.get(0).getPrn();
                    if (epochgps_observedSatellites.get(0).getFrequencyLable().equals("L1")) {

                        gpsSatellite.setPrn(prn);
                        gpsSatellite.setC1(epochgps_observedSatellites.get(i).getpseudorange());
                        gpsSatellite.setL1(epochgps_observedSatellites.get(i).getphase());
                        gpsSatellite.setS1(epochgps_observedSatellites.get(i).getSnr());
                        gpsSatellite.setD1(epochgps_observedSatellites.get(i).getDoppler());
                        positioningData.gpsSatelliteList.add(gpsSatellite);
                    }
                    if (epochgps_observedSatellites.get(0).getFrequencyLable().equals("L5")) {

                        gpsSatellite.setPrn(prn);
                        gpsSatellite.setC5(epochgps_observedSatellites.get(i).getpseudorange());
                        gpsSatellite.setL5(epochgps_observedSatellites.get(i).getphase());
                        gpsSatellite.setS5(epochgps_observedSatellites.get(i).getSnr());
                        gpsSatellite.setD5(epochgps_observedSatellites.get(i).getDoppler());
                        positioningData.gpsSatelliteList.add(gpsSatellite);
                    }
                }
                if (epochgps_observedSatellites.size() > 1) {
                    for (int i = 0; i < epochgps_observedSatellites.size(); i++) {

                        String prn = epochgps_observedSatellites.get(i).getPrn();
                        if (hasDoublesvid.contains(prn)) {
                            continue; // 已经处理过这个卫星
                        }
                        if(i==epochgps_observedSatellites.size()-1)
                        {
                            GpsSatellite gpsSatellite = new GpsSatellite();
                            if (epochgps_observedSatellites.get(0).getFrequencyLable().equals("L1")) {

                                gpsSatellite.setPrn(prn);
                                gpsSatellite.setC1(epochgps_observedSatellites.get(i).getpseudorange());
                                gpsSatellite.setL1(epochgps_observedSatellites.get(i).getphase());
                                gpsSatellite.setS1(epochgps_observedSatellites.get(i).getSnr());
                                gpsSatellite.setD1(epochgps_observedSatellites.get(i).getDoppler());
                                positioningData.gpsSatelliteList.add(gpsSatellite);
                            }
                            if (epochgps_observedSatellites.get(0).getFrequencyLable().equals("L5")) {

                                gpsSatellite.setPrn(prn);
                                gpsSatellite.setC5(epochgps_observedSatellites.get(i).getpseudorange());
                                gpsSatellite.setL5(epochgps_observedSatellites.get(i).getphase());
                                gpsSatellite.setS5(epochgps_observedSatellites.get(i).getSnr());
                                gpsSatellite.setD5(epochgps_observedSatellites.get(i).getDoppler());
                                positioningData.gpsSatelliteList.add(gpsSatellite);
                            }
                        }

                        for (int j = i + 1; j < epochgps_observedSatellites.size(); j++) {

                            GpsSatellite gpsSatellite = new GpsSatellite();
                         /*
                           表明这个卫星有两个频率
                         */
                            if (Objects.equals(epochgps_observedSatellites.get(i).getPrn(), epochgps_observedSatellites.get(j).getPrn())) {

                                hasDoublesvid.add(epochgps_observedSatellites.get(i).getPrn());
                            /*
                            表明L1频率在前，L5频率在后
                             */
                                if (epochgps_observedSatellites.get(i).getFrequency() > epochgps_observedSatellites.get(j).getFrequency()) {


                                    gpsSatellite.setPrn(prn);

                                    gpsSatellite.setC1(epochgps_observedSatellites.get(i).getpseudorange());
                                    gpsSatellite.setC5(epochgps_observedSatellites.get(j).getpseudorange());
                                    gpsSatellite.setP_IF();//双频

                                    gpsSatellite.setL1(epochgps_observedSatellites.get(i).getphase());
                                    gpsSatellite.setL5(epochgps_observedSatellites.get(j).getphase());

                                    gpsSatellite.setS1(epochgps_observedSatellites.get(i).getSnr());
                                    gpsSatellite.setS5(epochgps_observedSatellites.get(j).getSnr());

                                    gpsSatellite.setD1(epochgps_observedSatellites.get(i).getDoppler());
                                    gpsSatellite.setD5(epochgps_observedSatellites.get(j).getDoppler());
                                    positioningData.gpsSatelliteList.add(gpsSatellite);

                                    GNSSData gnssData=new GNSSData();
                                    gnssData.setPrn(prn);
                                    gnssData.setP_IF(epochgps_observedSatellites.get(i).getpseudorange(),epochgps_observedSatellites.get(j).getpseudorange());
                                    gnssData.setpseudorange(gnssData.getP_IF());
                                    positioningData.doubleFreeDatahash.put(prn,gnssData);
                                }
                            /*
                            表明L5频率在前，L1频率在后
                             */
                                if (epochgps_observedSatellites.get(i).getFrequency() < epochgps_observedSatellites.get(j).getFrequency()) {


                                    gpsSatellite.setPrn(prn);
                                    gpsSatellite.setC1(epochgps_observedSatellites.get(j).getpseudorange());
                                    gpsSatellite.setC5(epochgps_observedSatellites.get(i).getpseudorange());
                                    gpsSatellite.setP_IF();//双频
                                    gpsSatellite.setL1(epochgps_observedSatellites.get(j).getphase());
                                    gpsSatellite.setL5(epochgps_observedSatellites.get(i).getphase());
                                    gpsSatellite.setS1(epochgps_observedSatellites.get(j).getSnr());
                                    gpsSatellite.setS5(epochgps_observedSatellites.get(i).getSnr());
                                    gpsSatellite.setD1(epochgps_observedSatellites.get(j).getDoppler());
                                    gpsSatellite.setD5(epochgps_observedSatellites.get(i).getDoppler());
                                    positioningData.gpsSatelliteList.add(gpsSatellite);

                                    GNSSData gnssData=new GNSSData();
                                    gnssData.setPrn(prn);
                                    gnssData.setP_IF(epochgps_observedSatellites.get(j).getpseudorange(),epochgps_observedSatellites.get(i).getpseudorange());
                                    gnssData.setpseudorange(gnssData.getP_IF());
                                    positioningData.doubleFreeDatahash.put(prn,gnssData);
                                    //    Log.d("gps", "svid" + gpsSatellite.getPrn() + "  L1:" + gpsSatellite.getL1() + "  C1:" + gpsSatellite.getC1() + "  D1:" + gpsSatellite.getD1() + "  S1:" + gpsSatellite.getS1() + "  L5:" + gpsSatellite.getL5() + "  C5:" + gpsSatellite.getC5() + "  D5:" + gpsSatellite.getD5() + "  S5:" + gpsSatellite.getS5());
                                }
                                break;

                            }
                        /*
                        表明这个卫星只有一个频率
                         */
                            if (j == epochgps_observedSatellites.size() - 1 && !hasDoublesvid.contains(epochgps_observedSatellites.get(i).getPrn())) {


                                if (epochgps_observedSatellites.get(i).getFrequencyLable().equals("L1")) {

                                    gpsSatellite.setPrn(prn);
                                    gpsSatellite.setC1(epochgps_observedSatellites.get(i).getpseudorange());
                                    gpsSatellite.setL1(epochgps_observedSatellites.get(i).getphase());

                                    gpsSatellite.setS1(epochgps_observedSatellites.get(i).getSnr());

                                    gpsSatellite.setD1(epochgps_observedSatellites.get(i).getDoppler());
                                    positioningData.gpsSatelliteList.add(gpsSatellite);

                                    //     Log.d("gps", "svid" + gpsSatellite.getPrn() + "  L1:" + gpsSatellite.getL1() + "  C1:" + gpsSatellite.getC1() + "  D1:" + gpsSatellite.getD1() + "  S1:" + gpsSatellite.getS1() + "  L5:" + gpsSatellite.getL5() + "  C5:" + gpsSatellite.getC5() + "  D5:" + gpsSatellite.getD5() + "  S5:" + gpsSatellite.getS5());


                                }
                                if (epochgps_observedSatellites.get(i).getFrequencyLable().equals("L5")) {

                                    gpsSatellite.setPrn(prn);
                                    gpsSatellite.setC5(epochgps_observedSatellites.get(i).getpseudorange());
                                    gpsSatellite.setL5(epochgps_observedSatellites.get(i).getphase());

                                    gpsSatellite.setS5(epochgps_observedSatellites.get(i).getSnr());

                                    gpsSatellite.setD5(epochgps_observedSatellites.get(i).getDoppler());
                                    positioningData.gpsSatelliteList.add(gpsSatellite);

                                    //    Log.d("gps", "svid" + gpsSatellite.getPrn() + "  L1:" + gpsSatellite.getL1() + "  C1:" + gpsSatellite.getC1() + "  D1:" + gpsSatellite.getD1() + "  S1:" + gpsSatellite.getS1() + "  L5:" + gpsSatellite.getL5() + "  C5:" + gpsSatellite.getC5() + "  D5:" + gpsSatellite.getD5() + "  S5:" + gpsSatellite.getS5());

                                }


                            }
                        }


                    }
                }

                /**
                 * 处理qzss卫星
                 */
                if (epochqzss_observedSatellites.size() == 1) {
                    QzssSatellite qzssSatellite = new QzssSatellite();
                    int i = 0;

                    String remainingString = epochqzss_observedSatellites.get(0).getPrn().substring(1);
                    int number = Integer.parseInt(remainingString);
                    number -= 192;
                    String resultString = Integer.toString(number);
                    String prn = epochqzss_observedSatellites.get(0).getPrn().charAt(0) + resultString;


                    if (epochqzss_observedSatellites.get(i).getFrequencyLable().equals("L1")) {

                        qzssSatellite.setPrn(prn);
                        qzssSatellite.setC1(epochqzss_observedSatellites.get(i).getpseudorange());
                        qzssSatellite.setL1(epochqzss_observedSatellites.get(i).getphase());

                        qzssSatellite.setS1(epochqzss_observedSatellites.get(i).getSnr());

                        qzssSatellite.setD1(epochqzss_observedSatellites.get(i).getDoppler());
                        positioningData.qzssSatelliteList.add(qzssSatellite);

                        // Log.d("qzss", "svid" + qzssSatellite.getPrn() + "  L1:" + qzssSatellite.getL1() + "  C1:" + qzssSatellite.getC1() + "  D1:" + qzssSatellite.getD1() + "  S1:" + qzssSatellite.getS1() + "  L5:" + qzssSatellite.getL5() + "  C5:" + qzssSatellite.getC5() + "  D5:" + qzssSatellite.getD5() + "  S5:" + qzssSatellite.getS5());


                    }
                    if (epochqzss_observedSatellites.get(i).getFrequencyLable().equals("L5")) {

                        qzssSatellite.setPrn(prn);
                        qzssSatellite.setC5(epochqzss_observedSatellites.get(i).getpseudorange());
                        qzssSatellite.setL5(epochqzss_observedSatellites.get(i).getphase());

                        qzssSatellite.setS5(epochqzss_observedSatellites.get(i).getSnr());

                        qzssSatellite.setD5(epochqzss_observedSatellites.get(i).getDoppler());
                        positioningData.qzssSatelliteList.add(qzssSatellite);

                        //  Log.d("qzss", "svid" + qzssSatellite.getPrn() + "  L1:" + qzssSatellite.getL1() + "  C1:" + qzssSatellite.getC1() + "  D1:" + qzssSatellite.getD1() + "  S1:" + qzssSatellite.getS1() + "  L5:" + qzssSatellite.getL5() + "  C5:" + qzssSatellite.getC5() + "  D5:" + qzssSatellite.getD5() + "  S5:" + qzssSatellite.getS5());

                    }
                }
                if (epochqzss_observedSatellites.size() > 1) {
                    for (int i = 0; i < epochqzss_observedSatellites.size(); i++) {

                        String remainingString = epochqzss_observedSatellites.get(i).getPrn().substring(1);
                        int number = Integer.parseInt(remainingString);
                        number -= 192;
                        String resultString = Integer.toString(number);
                        String prn = epochqzss_observedSatellites.get(i).getPrn().charAt(0) + resultString;
                        if (hasDoublesvid.contains(prn)) {
                            continue; // 已经处理过这个卫星
                        }
                        if(i==epochqzss_observedSatellites.size()-1)
                        {
                            QzssSatellite qzssSatellite = new QzssSatellite();
                            if (epochqzss_observedSatellites.get(i).getFrequencyLable().equals("L1")) {

                                qzssSatellite.setPrn(prn);
                                qzssSatellite.setC1(epochqzss_observedSatellites.get(i).getpseudorange());
                                qzssSatellite.setL1(epochqzss_observedSatellites.get(i).getphase());

                                qzssSatellite.setS1(epochqzss_observedSatellites.get(i).getSnr());

                                qzssSatellite.setD1(epochqzss_observedSatellites.get(i).getDoppler());
                                positioningData.qzssSatelliteList.add(qzssSatellite);
                            }
                            if (epochqzss_observedSatellites.get(i).getFrequencyLable().equals("L5")) {

                                qzssSatellite.setPrn(prn);
                                qzssSatellite.setC5(epochqzss_observedSatellites.get(i).getpseudorange());
                                qzssSatellite.setL5(epochqzss_observedSatellites.get(i).getphase());

                                qzssSatellite.setS5(epochqzss_observedSatellites.get(i).getSnr());

                                qzssSatellite.setD5(epochqzss_observedSatellites.get(i).getDoppler());
                                positioningData.qzssSatelliteList.add(qzssSatellite);
                            }
                        }

                        for (int j = i + 1; j < epochqzss_observedSatellites.size(); j++) {

                            QzssSatellite qzssSatellite = new QzssSatellite();
                         /*
                           表明这个卫星有两个频率
                         */
                            if (Objects.equals(epochqzss_observedSatellites.get(i).getPrn(), epochqzss_observedSatellites.get(j).getPrn())) {

                                hasDoublesvid.add(epochqzss_observedSatellites.get(i).getPrn());
                            /*
                            表明L1频率在前，L5频率在后
                             */
                                if (epochqzss_observedSatellites.get(i).getFrequency() > epochqzss_observedSatellites.get(j).getFrequency()) {


                                    qzssSatellite.setPrn(prn);
                                    qzssSatellite.setC1(epochqzss_observedSatellites.get(i).getpseudorange());
                                    qzssSatellite.setC5(epochqzss_observedSatellites.get(j).getpseudorange());
                                    //qzssSatellite.setP_IF();//双频

                                    qzssSatellite.setL1(epochqzss_observedSatellites.get(i).getphase());
                                    qzssSatellite.setL5(epochqzss_observedSatellites.get(j).getphase());
                                    qzssSatellite.setS1(epochqzss_observedSatellites.get(i).getSnr());
                                    qzssSatellite.setS5(epochqzss_observedSatellites.get(j).getSnr());
                                    qzssSatellite.setD1(epochqzss_observedSatellites.get(i).getDoppler());
                                    qzssSatellite.setD5(epochqzss_observedSatellites.get(j).getDoppler());
                                    positioningData.qzssSatelliteList.add(qzssSatellite);

                                    GNSSData gnssData=new GNSSData();
                                    gnssData.setPrn(prn);
                                    gnssData.setP_IF(epochgps_observedSatellites.get(i).getpseudorange(),epochgps_observedSatellites.get(j).getpseudorange());
                                    gnssData.setpseudorange(gnssData.getP_IF());
                                    positioningData.doubleFreeDatahash.put(prn,gnssData);
                                    //      Log.d("qzss", "svid" + qzssSatellite.getPrn() + "  L1:" + qzssSatellite.getL1() + "  C1:" + qzssSatellite.getC1() + "  D1:" + qzssSatellite.getD1() + "  S1:" + qzssSatellite.getS1() + "  L5:" + qzssSatellite.getL5() + "  C5:" + qzssSatellite.getC5() + "  D5:" + qzssSatellite.getD5() + "  S5:" + qzssSatellite.getS5());
                                }
                            /*
                            表明L5频率在前，L1频率在后
                             */
                                if (epochqzss_observedSatellites.get(i).getFrequency() < epochqzss_observedSatellites.get(j).getFrequency()) {


                                    qzssSatellite.setPrn(prn);
                                    qzssSatellite.setC1(epochqzss_observedSatellites.get(j).getpseudorange());
                                    qzssSatellite.setC5(epochqzss_observedSatellites.get(i).getpseudorange());
                                    //qzssSatellite.setP_IF();//双频
                                    qzssSatellite.setL1(epochqzss_observedSatellites.get(j).getphase());
                                    qzssSatellite.setL5(epochqzss_observedSatellites.get(i).getphase());
                                    qzssSatellite.setS1(epochqzss_observedSatellites.get(j).getSnr());
                                    qzssSatellite.setS5(epochqzss_observedSatellites.get(i).getSnr());
                                    qzssSatellite.setD1(epochqzss_observedSatellites.get(j).getDoppler());
                                    qzssSatellite.setD5(epochqzss_observedSatellites.get(i).getDoppler());
                                    positioningData.qzssSatelliteList.add(qzssSatellite);

                                    GNSSData gnssData=new GNSSData();
                                    gnssData.setPrn(prn);
                                    gnssData.setP_IF(epochgps_observedSatellites.get(j).getpseudorange(),epochgps_observedSatellites.get(i).getpseudorange());
                                    gnssData.setpseudorange(gnssData.getP_IF());
                                    positioningData.doubleFreeDatahash.put(prn,gnssData);
                                    //    Log.d("qzss", "svid" + qzssSatellite.getPrn() + "  L1:" + qzssSatellite.getL1() + "  C1:" + qzssSatellite.getC1() + "  D1:" + qzssSatellite.getD1() + "  S1:" + qzssSatellite.getS1() + "  L5:" + qzssSatellite.getL5() + "  C5:" + qzssSatellite.getC5() + "  D5:" + qzssSatellite.getD5() + "  S5:" + qzssSatellite.getS5());
                                }
                                break;

                            }
                        /*
                        表明这个卫星只有一个频率
                         */
                            if (j == epochqzss_observedSatellites.size() - 1 && !hasDoublesvid.contains(epochqzss_observedSatellites.get(i).getPrn())) {
                                if (epochqzss_observedSatellites.get(i).getFrequencyLable().equals("L1")) {
                                    qzssSatellite.setPrn(prn);
                                    qzssSatellite.setC1(epochqzss_observedSatellites.get(i).getpseudorange());
                                    qzssSatellite.setL1(epochqzss_observedSatellites.get(i).getphase());
                                    qzssSatellite.setS1(epochqzss_observedSatellites.get(i).getSnr());
                                    qzssSatellite.setD1(epochqzss_observedSatellites.get(i).getDoppler());
                                    positioningData.qzssSatelliteList.add(qzssSatellite);
                                }
                                if (epochqzss_observedSatellites.get(i).getFrequencyLable().equals("L5")) {
                                    qzssSatellite.setPrn(prn);
                                    qzssSatellite.setC5(epochqzss_observedSatellites.get(i).getpseudorange());
                                    qzssSatellite.setL5(epochqzss_observedSatellites.get(i).getphase());
                                    qzssSatellite.setS5(epochqzss_observedSatellites.get(i).getSnr());
                                    qzssSatellite.setD5(epochqzss_observedSatellites.get(i).getDoppler());
                                    positioningData.qzssSatelliteList.add(qzssSatellite);
                                }
                            }
                        }


                    }
                }

                /**
                 * 处理galileo卫星
                 */
                if (epochgalileo_observedSatellites.size() == 1) {

                     /*
                      表明只有一个卫星，只有一个频率
                     */
                    String prn=epochgalileo_observedSatellites.get(0).getPrn();
                    int i = 0;
                    GalileoSatellite galileoSatellite = new GalileoSatellite();


                    if (epochgalileo_observedSatellites.get(i).getFrequencyLable().equals("L1")) {
                        galileoSatellite.setPrn(prn);
                        galileoSatellite.setC1(epochgalileo_observedSatellites.get(i).getpseudorange());
                        galileoSatellite.setL1(epochgalileo_observedSatellites.get(i).getphase());
                        galileoSatellite.setS1(epochgalileo_observedSatellites.get(i).getSnr());
                        galileoSatellite.setD1(epochgalileo_observedSatellites.get(i).getDoppler());
                        positioningData.galileoSatelliteList.add(galileoSatellite);
                    }
                    if (epochgalileo_observedSatellites.get(i).getFrequencyLable().equals("L5")) {
                        galileoSatellite.setPrn(prn);
                        galileoSatellite.setC5(epochgalileo_observedSatellites.get(i).getpseudorange());
                        galileoSatellite.setL5(epochgalileo_observedSatellites.get(i).getphase());
                        galileoSatellite.setS5(epochgalileo_observedSatellites.get(i).getSnr());
                        galileoSatellite.setD5(epochgalileo_observedSatellites.get(i).getDoppler());
                        positioningData.galileoSatelliteList.add(galileoSatellite);
                    }
                }

                if (epochgalileo_observedSatellites.size() > 1) {
                    for (int i = 0; i < epochgalileo_observedSatellites.size(); i++) {

                        String prn=epochgalileo_observedSatellites.get(i).getPrn();
                        if (hasDoublesvid.contains(prn)) {
                            continue; // 已经处理过这个卫星
                        }
                        if(i==epochgalileo_observedSatellites.size()-1)
                        {
                            GalileoSatellite galileoSatellite = new GalileoSatellite();
                            if (epochgalileo_observedSatellites.get(i).getFrequencyLable().equals("L1")) {
                                galileoSatellite.setPrn(prn);
                                galileoSatellite.setC1(epochgalileo_observedSatellites.get(i).getpseudorange());
                                galileoSatellite.setL1(epochgalileo_observedSatellites.get(i).getphase());
                                galileoSatellite.setS1(epochgalileo_observedSatellites.get(i).getSnr());
                                galileoSatellite.setD1(epochgalileo_observedSatellites.get(i).getDoppler());
                                positioningData.galileoSatelliteList.add(galileoSatellite);
                            }
                            if (epochgalileo_observedSatellites.get(i).getFrequencyLable().equals("L5")) {
                                galileoSatellite.setPrn(prn);
                                galileoSatellite.setC5(epochgalileo_observedSatellites.get(i).getpseudorange());
                                galileoSatellite.setL5(epochgalileo_observedSatellites.get(i).getphase());
                                galileoSatellite.setS5(epochgalileo_observedSatellites.get(i).getSnr());
                                galileoSatellite.setD5(epochgalileo_observedSatellites.get(i).getDoppler());
                                positioningData.galileoSatelliteList.add(galileoSatellite);
                            }
                        }
                        for (int j = i + 1; j < epochgalileo_observedSatellites.size(); j++) {

                            GalileoSatellite galileoSatellite = new GalileoSatellite();
                         /*
                           表明这个卫星有两个频率
                         */
                            if (Objects.equals(epochgalileo_observedSatellites.get(i).getPrn(), epochgalileo_observedSatellites.get(j).getPrn())) {

                                hasDoublesvid.add(epochgalileo_observedSatellites.get(i).getPrn());
                            /*
                            表明L1频率在前，L5频率在后
                             */
                                if (epochgalileo_observedSatellites.get(i).getFrequency() > epochgalileo_observedSatellites.get(j).getFrequency()) {


                                    galileoSatellite.setPrn(prn);
                                    galileoSatellite.setC1(epochgalileo_observedSatellites.get(i).getpseudorange());
                                    galileoSatellite.setC5(epochgalileo_observedSatellites.get(j).getpseudorange());

                                    galileoSatellite.setL1(epochgalileo_observedSatellites.get(i).getphase());
                                    galileoSatellite.setL5(epochgalileo_observedSatellites.get(j).getphase());
                                    galileoSatellite.setS1(epochgalileo_observedSatellites.get(i).getSnr());
                                    galileoSatellite.setS5(epochgalileo_observedSatellites.get(j).getSnr());
                                    galileoSatellite.setD1(epochgalileo_observedSatellites.get(i).getDoppler());
                                    galileoSatellite.setD5(epochgalileo_observedSatellites.get(j).getDoppler());
                                    positioningData.galileoSatelliteList.add(galileoSatellite);

                                    GNSSData gnssData=new GNSSData();
                                    gnssData.setPrn(prn);
                                    gnssData.setP_IF(epochgps_observedSatellites.get(i).getpseudorange(),epochgps_observedSatellites.get(j).getpseudorange());
                                    gnssData.setpseudorange(gnssData.getP_IF());
                                    positioningData.doubleFreeDatahash.put(prn,gnssData);
                                    //       Log.d(" galileo", "svid" + galileoSatellite.getPrn() + "  L1:" + galileoSatellite.getL1() + "  C1:" + galileoSatellite.getC1() + "  D1:" + galileoSatellite.getD1() + "  S1:" + galileoSatellite.getS1() + "  L5:" + galileoSatellite.getL5() + "  C5:" + galileoSatellite.getC5() + "  D5:" + galileoSatellite.getD5() + "  S5:" + galileoSatellite.getS5());
                                }
                            /*
                            表明L5频率在前，L1频率在后
                             */
                                if (epochgalileo_observedSatellites.get(i).getFrequency() < epochgalileo_observedSatellites.get(j).getFrequency()) {


                                    galileoSatellite.setPrn(prn);
                                    galileoSatellite.setC1(epochgalileo_observedSatellites.get(j).getpseudorange());
                                    galileoSatellite.setC5(epochgalileo_observedSatellites.get(i).getpseudorange());
                                    galileoSatellite.setL1(epochgalileo_observedSatellites.get(j).getphase());
                                    galileoSatellite.setL5(epochgalileo_observedSatellites.get(i).getphase());
                                    galileoSatellite.setS1(epochgalileo_observedSatellites.get(j).getSnr());
                                    galileoSatellite.setS5(epochgalileo_observedSatellites.get(i).getSnr());
                                    galileoSatellite.setD1(epochgalileo_observedSatellites.get(j).getDoppler());
                                    galileoSatellite.setD5(epochgalileo_observedSatellites.get(i).getDoppler());
                                    positioningData.galileoSatelliteList.add(galileoSatellite);

                                    GNSSData gnssData=new GNSSData();
                                    gnssData.setPrn(prn);
                                    gnssData.setP_IF(epochgps_observedSatellites.get(j).getpseudorange(),epochgps_observedSatellites.get(i).getpseudorange());
                                    gnssData.setpseudorange(gnssData.getP_IF());
                                    positioningData.doubleFreeDatahash.put(prn,gnssData);
                                    //    Log.d(" galileo", "svid" + galileoSatellite.getPrn() + "  L1:" + galileoSatellite.getL1() + "  C1:" + galileoSatellite.getC1() + "  D1:" + galileoSatellite.getD1() + "  S1:" + galileoSatellite.getS1() + "  L5:" + galileoSatellite.getL5() + "  C5:" + galileoSatellite.getC5() + "  D5:" + galileoSatellite.getD5() + "  S5:" + galileoSatellite.getS5());
                                }
                                break;

                            }
                        /*
                        表明这个卫星只有一个频率
                         */
                            if (j == epochgalileo_observedSatellites.size() - 1 && !hasDoublesvid.contains(epochgalileo_observedSatellites.get(i).getPrn())) {


                                if (epochgalileo_observedSatellites.get(i).getFrequencyLable().equals("L1")) {

                                    galileoSatellite.setPrn(prn);
                                    galileoSatellite.setC1(epochgalileo_observedSatellites.get(i).getpseudorange());
                                    galileoSatellite.setL1(epochgalileo_observedSatellites.get(i).getphase());

                                    galileoSatellite.setS1(epochgalileo_observedSatellites.get(i).getSnr());

                                    galileoSatellite.setD1(epochgalileo_observedSatellites.get(i).getDoppler());
                                    positioningData.galileoSatelliteList.add(galileoSatellite);

                                    //    Log.d("galileo", "svid" + galileoSatellite.getPrn() + "  L1:" + galileoSatellite.getL1() + "  C1:" + galileoSatellite.getC1() + "  D1:" + galileoSatellite.getD1() + "  S1:" + galileoSatellite.getS1() + "  L5:" + galileoSatellite.getL5() + "  C5:" + galileoSatellite.getC5() + "  D5:" + galileoSatellite.getD5() + "  S5:" + galileoSatellite.getS5());


                                }
                                if (epochgalileo_observedSatellites.get(i).getFrequencyLable().equals("L5")) {

                                    galileoSatellite.setPrn(prn);
                                    galileoSatellite.setC5(epochgalileo_observedSatellites.get(i).getpseudorange());
                                    galileoSatellite.setL5(epochgalileo_observedSatellites.get(i).getphase());

                                    galileoSatellite.setS5(epochgalileo_observedSatellites.get(i).getSnr());

                                    galileoSatellite.setD5(epochgalileo_observedSatellites.get(i).getDoppler());
                                    positioningData.galileoSatelliteList.add(galileoSatellite);

                                    //    Log.d("galileo", "svid" + galileoSatellite.getPrn() + "  L1:" + galileoSatellite.getL1() + "  C1:" + galileoSatellite.getC1() + "  D1:" + galileoSatellite.getD1() + "  S1:" + galileoSatellite.getS1() + "  L5:" + galileoSatellite.getL5() + "  C5:" + galileoSatellite.getC5() + "  D5:" + galileoSatellite.getD5() + "  S5:" + galileoSatellite.getS5());

                                }


                            }
                        }


                    }
                }

                /**
                 * 处理bds卫星,,,bds卫星只有三个频率   B1，L1，L5
                 */
                ArrayList<GNSSData> B1Data=new ArrayList<>();
                ArrayList<GNSSData> L1Data=new ArrayList<>();
                ArrayList<GNSSData> L5Data=new ArrayList<>();
                ArrayList<String> hasContainedPrn = new ArrayList<>();
                for(int i=0;i<epochbds_observedSatellites.size();i++){
                    String frequencyLable=epochbds_observedSatellites.get(i).getFrequencyLable();
                    if(Objects.equals(frequencyLable, "B1"))
                    {
                        B1Data.add(epochbds_observedSatellites.get(i));
                    }
                    else if(Objects.equals(frequencyLable, "L1"))
                    {
                        L1Data.add(epochbds_observedSatellites.get(i));
                    }
                    else if(Objects.equals(frequencyLable, "L5"))
                    {
                        L5Data.add(epochbds_observedSatellites.get(i));
                    }
                }

                if(epochbds_observedSatellites.size()==1){
                    BdsSatellite bdsSatellite=new BdsSatellite();
                    int i=0;
                    if(Objects.equals(epochbds_observedSatellites.get(i).getFrequencyLable(), "B1")){
                        bdsSatellite.setPrn(epochbds_observedSatellites.get(i).getPrn());
                        bdsSatellite.setC2I(epochbds_observedSatellites.get(i).getpseudorange());
                        bdsSatellite.setL2I(epochbds_observedSatellites.get(i).getphase());
                        bdsSatellite.setS2I(epochbds_observedSatellites.get(i).getSnr());
                        bdsSatellite.setD2I(epochbds_observedSatellites.get(i).getDoppler());
                        positioningData.bdsSatelliteList.add(bdsSatellite);

                    }
                    if(Objects.equals(epochbds_observedSatellites.get(i).getFrequencyLable(), "L1")){
                        bdsSatellite.setPrn(epochbds_observedSatellites.get(i).getPrn());
                        bdsSatellite.setC1(epochbds_observedSatellites.get(i).getpseudorange());
                        bdsSatellite.setL1(epochbds_observedSatellites.get(i).getphase());
                        bdsSatellite.setS1(epochbds_observedSatellites.get(i).getSnr());
                        bdsSatellite.setD1(epochbds_observedSatellites.get(i).getDoppler());
                        positioningData.bdsSatelliteList.add(bdsSatellite);

                    }
                    if(Objects.equals(epochbds_observedSatellites.get(i).getFrequencyLable(), "L5")){
                        bdsSatellite.setPrn(epochbds_observedSatellites.get(i).getPrn());
                        bdsSatellite.setC5(epochbds_observedSatellites.get(i).getpseudorange());
                        bdsSatellite.setL5(epochbds_observedSatellites.get(i).getphase());
                        bdsSatellite.setS5(epochbds_observedSatellites.get(i).getSnr());
                        bdsSatellite.setD5(epochbds_observedSatellites.get(i).getDoppler());
                        positioningData.bdsSatelliteList.add(bdsSatellite);

                    }
                }
                else if(epochbds_observedSatellites.size()>1){
                    for(int i=0;i<epochbds_observedSatellites.size();i++)
                    {
                        String prn=epochbds_observedSatellites.get(i).getPrn();
                        if(hasContainedPrn.contains(prn)){
                            continue;
                        }
                        else {
                            hasContainedPrn.add(prn);
                        }
                        BdsSatellite bdsSatellite=new BdsSatellite();
                        bdsSatellite.setPrn(prn);
                        String frequencyLable=epochbds_observedSatellites.get(i).getFrequencyLable();
                        if(Objects.equals(frequencyLable, "B1"))
                        {
                            bdsSatellite.setC2I(epochbds_observedSatellites.get(i).getpseudorange());
                            bdsSatellite.setL2I(epochbds_observedSatellites.get(i).getphase());
                            bdsSatellite.setS2I(epochbds_observedSatellites.get(i).getSnr());
                            bdsSatellite.setD2I(epochbds_observedSatellites.get(i).getDoppler());
                            for (int j=0;j<L1Data.size();j++){
                                if (Objects.equals(L1Data.get(j).getPrn(), prn)){
                                    bdsSatellite.setC1(epochbds_observedSatellites.get(j).getpseudorange());
                                    bdsSatellite.setL1(epochbds_observedSatellites.get(j).getphase());
                                    bdsSatellite.setS1(epochbds_observedSatellites.get(j).getSnr());
                                    bdsSatellite.setD1(epochbds_observedSatellites.get(j).getDoppler());
                                    break;
                                }
                            }
                            for (int k=0;k<L5Data.size();k++){
                                if (Objects.equals(L5Data.get(k).getPrn(), prn)){
                                    bdsSatellite.setC5(epochbds_observedSatellites.get(k).getpseudorange());
                                    bdsSatellite.setL5(epochbds_observedSatellites.get(k).getphase());
                                    bdsSatellite.setS5(epochbds_observedSatellites.get(k).getSnr());
                                    bdsSatellite.setD5(epochbds_observedSatellites.get(k).getDoppler());
                                    break;
                                }
                            }
                            if(bdsSatellite.isHasC1()&&bdsSatellite.isHasC5())
                            {
                                bdsSatellite.setP_IF();

                                GNSSData gnssData=new GNSSData();
                                gnssData.setPrn(prn);
                                gnssData.setpseudorange(bdsSatellite.getP_IF());
                                positioningData.doubleFreeDatahash.put(prn,gnssData);
                            }
                            positioningData.bdsSatelliteList.add(bdsSatellite);

                        }
                        else if (Objects.equals(frequencyLable, "L1"))
                        {
                            bdsSatellite.setC1(epochbds_observedSatellites.get(i).getpseudorange());
                            bdsSatellite.setL1(epochbds_observedSatellites.get(i).getphase());
                            bdsSatellite.setS1(epochbds_observedSatellites.get(i).getSnr());
                            bdsSatellite.setD1(epochbds_observedSatellites.get(i).getDoppler());
                            for (int j=0;j<B1Data.size();j++){
                                if (Objects.equals(B1Data.get(j).getPrn(), prn)){
                                    bdsSatellite.setC2I(epochbds_observedSatellites.get(j).getpseudorange());
                                    bdsSatellite.setL2I(epochbds_observedSatellites.get(j).getphase());
                                    bdsSatellite.setS2I(epochbds_observedSatellites.get(j).getSnr());
                                    bdsSatellite.setD2I(epochbds_observedSatellites.get(j).getDoppler());
                                    break;
                                }
                            }
                            for (int k=0;k<L5Data.size();k++){
                                if (Objects.equals(L5Data.get(k).getPrn(), prn)){
                                    bdsSatellite.setC5(epochbds_observedSatellites.get(k).getpseudorange());
                                    bdsSatellite.setL5(epochbds_observedSatellites.get(k).getphase());
                                    bdsSatellite.setS5(epochbds_observedSatellites.get(k).getSnr());
                                    bdsSatellite.setD5(epochbds_observedSatellites.get(k).getDoppler());
                                    break;
                                }
                            }
                            if(bdsSatellite.isHasC1()&&bdsSatellite.isHasC5())
                            {
                                bdsSatellite.setP_IF();

                                GNSSData gnssData=new GNSSData();
                                gnssData.setPrn(prn);
                                gnssData.setpseudorange(bdsSatellite.getP_IF());
                                positioningData.doubleFreeDatahash.put(prn,gnssData);
                            }
                            positioningData.bdsSatelliteList.add(bdsSatellite);

                        }
                        else if (Objects.equals(frequencyLable, "L5"))
                        {
                            bdsSatellite.setC5(epochbds_observedSatellites.get(i).getpseudorange());
                            bdsSatellite.setL5(epochbds_observedSatellites.get(i).getphase());
                            bdsSatellite.setS5(epochbds_observedSatellites.get(i).getSnr());
                            bdsSatellite.setD5(epochbds_observedSatellites.get(i).getDoppler());
                            for (int j=0;j<L1Data.size();j++){
                                if (Objects.equals(L1Data.get(j).getPrn(), prn)){
                                    bdsSatellite.setC1(epochbds_observedSatellites.get(j).getpseudorange());
                                    bdsSatellite.setL1(epochbds_observedSatellites.get(j).getphase());
                                    bdsSatellite.setS1(epochbds_observedSatellites.get(j).getSnr());
                                    bdsSatellite.setD1(epochbds_observedSatellites.get(j).getDoppler());
                                    break;
                                }
                            }
                            for (int k=0;k<B1Data.size();k++){
                                if (Objects.equals(B1Data.get(k).getPrn(), prn)){
                                    bdsSatellite.setC2I(epochbds_observedSatellites.get(k).getpseudorange());
                                    bdsSatellite.setL2I(epochbds_observedSatellites.get(k).getphase());
                                    bdsSatellite.setS2I(epochbds_observedSatellites.get(k).getSnr());
                                    bdsSatellite.setD2I(epochbds_observedSatellites.get(k).getDoppler());
                                    break;
                                }
                            }
                            if(bdsSatellite.isHasC1()&&bdsSatellite.isHasC5())
                            {
                                bdsSatellite.setP_IF();

                                GNSSData gnssData=new GNSSData();
                                gnssData.setPrn(prn);
                                gnssData.setpseudorange(bdsSatellite.getP_IF());
                                positioningData.doubleFreeDatahash.put(prn,gnssData);
                            }
                            positioningData.bdsSatelliteList.add(bdsSatellite);

                        }

                    }
                }

                B1Data=null;
                L1Data=null;
                L5Data=null;
                hasContainedPrn=null;
                /**
                 * 处理glonass卫星,,,glonass卫星只有一个频率   R1
                 */
                for (int i = 0; i < epochglonass_observedSatellites.size(); i++) {
                    String prn = epochglonass_observedSatellites.get(i).getPrn();

                    /**
                     * 表明带的是R1频率
                     */

                    GlonassSatellite glonassSatellite = new GlonassSatellite();

                    glonassSatellite.setPrn(prn);

                    glonassSatellite.setC1C(epochglonass_observedSatellites.get(i).getpseudorange());

                    glonassSatellite.setL1C(epochglonass_observedSatellites.get(i).getphase());

                    glonassSatellite.setD1C(epochglonass_observedSatellites.get(i).getDoppler());

                    glonassSatellite.setS1C(epochglonass_observedSatellites.get(i).getSnr());

                    positioningData.glonassSatelliteList.add(glonassSatellite);



                    //    Log.d("glonass", "svid : " + glonassSatellite.getPrn() + "  C1C: " + glonassSatellite.getC1C() + "  L1C:" + glonassSatellite.getL1C() + "  D1C:" + glonassSatellite.getD1C() + "  S1C" + glonassSatellite.getS1C());

                }

                System.out.println("here");
                hasDoublesvid.clear();
            }


        }
        catch (Exception e) {
            sendUiUpdateMessage("调试","原始数据转换为历元数据出错，可能为原始数据为空");
        }

    }





    /**
     * 对于卫星的prn不足长度  补零的方法
     *
     * @param constellationLabel 系统标签   如 G   J    C   R   E
     * @param svid               messurement.getsvid()
     * @return
     */
    private String addprn(char constellationLabel, int svid) {

        @SuppressLint("DefaultLocale") String prn = String.format("%c%02d", constellationLabel, svid);
        return prn;
    }
    private String addprn(char constellationLabel, int svid, String lable) {
        @SuppressLint("DefaultLocale") String prn = String.format("%c%02d%s", constellationLabel, svid, lable);
        return prn;
    }
    public void init()
    {
        try {
            System.out.println("星历");
            //mrinexNavigationM.getFromFTP(NASA_NAVIGATION_HOURLY);
            mrinexNavigationMnew.getFromFTP(WHU_GPS_HOURLY);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void calculateSatPosition( Coordinates position) {

        // Make a list to hold the satellites that are to be excluded based on elevation/CN0 masking criteria
        //根据给定的 RINEX 导航文件 和接收机位置  计算每个观测卫星的位置，并根据仰角掩码标准排除不符合条件的卫星。


        synchronized (this) {

            //positioningData.computDataList.clear();
            testList2.clear();


            for (GNSSData gnssData : testList) {
                // Computation of the GPS satellite coordinates in ECEF frame
                //计算GPS卫星在ECEF坐标系中的坐标 地心地固系

                //观测数据的时间
                // Determine the current GPS week number
                int gpsWeek = (int) (weekNumberNanos / Constants.NUMBER_NANO_SECONDS_PER_WEEK);

                // Time of signal reception in GPS Seconds of the Week (SoW)
                double gpsSow = (tRxGPS - weekNumberNanos) * 1e-9;
                Time tGPS = new Time(gpsWeek, gpsSow);

                // Convert the time of reception from GPS SoW to UNIX time (milliseconds)
                long timeRx = tGPS.getMsec();//UNIX time (milliseconds)

                SatellitePosition sp = mrinexNavigationMnew.getSatPositionAndVelocities(
                        timeRx,//观测数据的时间
                        gnssData.getpseudorange(),
                        gnssData.getSATID(),
                        gnssData.getGnssType(),
                        0.0
                );

                if (sp == null) {
                    continue;
                }

                gnssData.setSp(sp);

                //设置卫星相对于用户的方位角和仰角，并根据这些角度来设置伪距测量方差
                gnssData.setRxTopo(
                        new TopocentricCoordinates(
                                position,
                                gnssData.getSp()));

                //Add to the exclusion list the satellites that do not pass the masking criteria
                if (gnssData.getRxTopo().getElevation() < MASK_ELEVATION) {
//                    continue;
                }

                //计算累计的误差，包括对流层延迟和电离层延迟
                //遍历计算三种误差并累加
                ArrayList<Correction> corrections = new ArrayList<>();
                IonoCorrection ionoCorrection=new IonoCorrection();
                TropoCorrection tropoCorrection=new TropoCorrection();
                ShapiroCorrection shapiroCorrection=new ShapiroCorrection();
                corrections.add(ionoCorrection);
                corrections.add(tropoCorrection);
                corrections.add(shapiroCorrection);
                double accumulatedCorrection = 0;


                for (Correction correction : corrections) {

                    correction.calculateCorrection(
                            new Time(timeRx),
                            position,
                            gnssData.getSp(),
                            mrinexNavigationMnew);

                    accumulatedCorrection += correction.getCorrection();

                }

                gnssData.setAccumulatedCorrection(accumulatedCorrection);

                //positioningData.computDataList.add(gnssData);
                testList2.add(gnssData);
            }


        }
    }


    private void sendUiUpdateMessage(String key, String value) {
        Message msg = uiHandler.obtainMessage();
        Bundle data = new Bundle();
        data.putString(key, value);
        msg.setData(data);
        uiHandler.sendMessage(msg);
    }


}

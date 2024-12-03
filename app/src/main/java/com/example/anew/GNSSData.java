package com.example.anew;

import com.example.anew.coord.SatellitePosition;
import com.example.anew.corrections.TopocentricCoordinates;

import org.ejml.simple.SimpleMatrix;

public class GNSSData{//GNSS数据类
    private char GnssType;
    private int SATID;//卫星ID
    private double frequency;
    private String frequencyLable;
    private String prn;
    private String prnAndF;
    private double snr;//信噪比

    private double doppler;

    private int SATstate;//卫星是否参与解算，0不参与，1参与
    private SatellitePosition sp;
    private double satelliteClockError; //卫星钟误差
    private long unixTime;
    private SimpleMatrix SATECEF;//卫星ECEF系的坐标
    private SimpleMatrix SATspeed;//卫星速度，矩阵存放
    private double var;//卫星位置和钟的方差
    private double recevierX;//接收机位置X，ecef
    private double recevierY;
    private double recevierZ;
    private double accumulatedCorrection;
    private TopocentricCoordinates rxTopo;
    private double measurementVariance ;
    private double dtrGPS;//GPS接收机钟差
    private double zwd;//对流层湿延迟
    private double n;//浮点模糊度，并不是所有的方程都有，没有的置为0
    private double frontresiduals;//先验残差
    private double afterresiduals;//后验残差
    private double phase;//载波相位观测值

    private double pseudorange;//伪距观测值

//        private double pseudorangeFree;
//        private double PseudorangeSmooth;

    public void setGnssType(char type){this.GnssType=type;}
    public void setSATID(int SATID){
        this.SATID = SATID;
    }
    public void setFrequency(double frequency){this.frequency=frequency;}
    public void setFrequencyLable(String lable){this.frequencyLable=lable;}
    public void setPrn(String prn) {this.prn = prn;}


    public void setSATstate(int SATstate){
        this.SATstate = SATstate;
    }
    public void setsatelliteClockError(double satelliteClockError){
        this.satelliteClockError = satelliteClockError;
    }
    public void setunixTime(long unixTime){
        this.unixTime = unixTime;
    }
    public void setSATECEF(SimpleMatrix SATECEF){
        this.SATECEF =SATECEF;
    }
    public void setSATspeed(SimpleMatrix SATspeed){
        this.SATspeed =SATspeed;
    }
    public void setvar(double var){
        this.var = var;
    }
    public void setrecevierX(double recevierX){
        this.recevierX = recevierX;
    }
    public void setrecevierY(double recevierY){
        this.recevierY = recevierY;
    }
    public void setrecevierZ(double recevierZ){
        this.recevierZ = recevierZ;
    }
    public void setdtrGPS(double dtrGPS){
        this.dtrGPS = dtrGPS;
    }
    public void setzwd(double zwd){
        this.zwd = zwd;
    }
    public void setn(double n){
        this.n = n;
    }
    public void setfrontresiduals(double frontresiduals){this.frontresiduals = frontresiduals;}
    public void setafterresiduals(double afterresiduals){this.afterresiduals = afterresiduals;}
    public void setphase(double phase){this.phase = phase;}
    public void setpseudorange(double pseudorange){this.pseudorange = pseudorange;}

    public char getGnssType(){return this.GnssType;}
    public int getSATID(){return this.SATID;}
    public double getFrequency(){return this.frequency;}
    public String getFrequencyLable(){return this.frequencyLable;}
    public String getPrn() {
        return prn;
    }
    public int getSATstate(){return this.SATstate;}
    public double getsatelliteClockError(){return this.satelliteClockError;}
    public long getunixTime(){return this.unixTime;}
    public SimpleMatrix getSATECEF(){return this.SATECEF;}
    public SimpleMatrix getSATspeed(){return this.SATspeed;}
    public double getvar(){return this.var;}
    public double getrecevierX(){return this.recevierX;}
    public double getrecevierY(){return this.recevierY;}
    public double getrecevierZ(){return this.recevierZ;}
    public double getdtrGPS(){return this.dtrGPS;}
    public double getzwd(){return this.zwd;}
    public double getn(){return this.n;}
    public double getfrontresiduals(){return this.frontresiduals;}
    public double getafterresiduals(){return this.afterresiduals;}
    public double getphase(){return this.phase;}
    public double getpseudorange(){return this.pseudorange;}

    public double getSnr() {return snr;}
    public void setSnr(double snr) {this.snr = snr;}

    public double getDoppler() {
        return doppler;
    }

    public void setDoppler(double doppler) {
        this.doppler = doppler;
    }




    private double P_IF;

    public double getP_IF() {
        return P_IF;
    }

    public void setP_IF(double pseudorangeL1,double pseudorangeL5) {
        double f1_squared = GNSSConstants.FL1 * GNSSConstants.FL1;
        double f5_squared = GNSSConstants.FL5 * GNSSConstants.FL5;
        double denominator = f1_squared - f5_squared;
        P_IF = (f1_squared / denominator) * pseudorangeL1 - (f5_squared / denominator) * pseudorangeL5;
    }


    public String getPrnAndF() {
        return prnAndF;
    }

    public void setPrnAndF(String prnAndF) {
        this.prnAndF = prnAndF;
    }

    public double getAccumulatedCorrection() {
        return accumulatedCorrection;
    }

    public void setAccumulatedCorrection(double accumulatedCorrection) {
        this.accumulatedCorrection = accumulatedCorrection;
    }
    public TopocentricCoordinates getRxTopo() {
        return rxTopo;
    }
    public void setRxTopo(TopocentricCoordinates rxTopo) { //todo rename to better indicate function
        this.rxTopo = rxTopo;
        this.measurementVariance= 1.0 / Math.pow(Math.tan(rxTopo.getElevation()-0.1),2)/100.0;
    }

    public SatellitePosition getSp() {
        return sp;
    }

    public void setSp(SatellitePosition sp) {
        this.sp = sp;
    }
}
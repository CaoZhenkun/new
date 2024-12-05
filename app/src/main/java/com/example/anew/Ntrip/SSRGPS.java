package com.example.anew.Ntrip;

import static com.example.anew.Constants.MAXCODE;

import com.example.anew.Constellations.Time;

public class SSRGPS {

    private char satType; /* Satellite Type */

    private int satID; /* Satellite ID number */

    private Time ephTime;//星历GPS时

    private Time clkTime;

    private Time hrclkTime;

    private Time uraTime;

    private Time biasTime;

    private Time pbiasTime;

    public Time getEphTime() {
        return ephTime;
    }

    public void setEphTime(Time ephTime) {
        this.ephTime = ephTime;
    }

    public Time getClkTime() {
        return clkTime;
    }

    public void setClkTime(Time clkTime) { this.clkTime = clkTime; }

    public Time getHrclkTime() {
        return hrclkTime;
    }

    public void setHrclkTime(Time hrclkTime) {
        this.hrclkTime = hrclkTime;
    }

    public Time getUraTime() {
        return uraTime;
    }

    public void setUraTime(Time uraTime) {
        this.uraTime = uraTime;
    }

    public Time getBiasTime() {
        return biasTime;
    }

    public void setBiasTime(Time biasTime) {
        this.biasTime = biasTime;
    }

    public Time getPbiasTime() {
        return pbiasTime;
    }

    public void setPbiasTime(Time pbiasTime) {
        this.pbiasTime = pbiasTime;
    }

    private double[] udi = new double[6];      /* SSR update interval (s) */

    private double[] iod = new double[6];         /* iod ssr {eph,clk,hrclk,ura,bias,pbias} */

    private int iode;           /* issue of data */

    private int iodcrc;         /* issue of data crc for beidou/sbas */

    private int ura;            /* URA indicator */

    private int refd;           /* sat ref datum (0:ITRF,1:regional) */

    private double[] deph = new double[3];    /* delta orbit {radial,along,cross} (m) */

    private double[] ddeph = new double[3];   /* dot delta orbit {radial,along,cross} (m/s) */

    private double[] dclk = new double[3];    /* delta clock {c0,c1,c2} (m,m/s,m/s^2) */

    private double hrclk;       /* high-rate clock corection (m) */

    private double[] cbias = new double[MAXCODE]; /* code biases (m) */

    private double[] pbias = new double[MAXCODE]; /* phase biases (m) */

    private float[] stdpb = new float[MAXCODE]; /* std-dev of phase biases (m) */

    private double yaw_ang,yaw_rate; /* yaw angle and yaw rate (deg,deg/s) */

    public char getSatType() {
        return satType;
    }

    public void setSatType(char satType) {
        this.satType = satType;
    }

    public int getSatID() {
        return satID;
    }

    public void setSatID(int satID) {
        this.satID = satID;
    }

    public void setUdi(double[] udi,int i) {
        this.udi[i] = udi[i];
    }

    public double getUdi(int i) {
        return udi[i];
    }

    public void setCbias(double[] cbias,int i) {
        this.cbias[i] = cbias[i];
    }

    public double getCbias(int i) {
        return cbias[i];
    }

    public void setDclk(double[] dclk,int i) {
        this.dclk[i] = dclk[i];
    }

    public double getDclk(int i) {
        return dclk[i];
    }

    public void setDdeph(double[] ddeph,int i) {
        this.ddeph[i] = ddeph[i];
    }

    public double getDdeph(int i) {
        return ddeph[i];
    }

    public void setDeph(double[] deph,int i) {
        this.deph[i] = deph[i];
    }

    public double getDeph(int i) {
        return deph[i];
    }

    public void setHrclk(double hrclk) {
        this.hrclk = hrclk;
    }

    public double getHrclk() {
        return hrclk;
    }

    public void setIod(double[] iod,int i) {
        this.iod[i] = iod[i];
    }

    public double getIod(int i) {
        return iod[i];
    }

    public void setIodcrc(int iodcrc) {
        this.iodcrc = iodcrc;
    }

    public int getIodcrc() {
        return iodcrc;
    }

    public void setIode(int iode) {
        this.iode = iode;
    }

    public int getIode() {
        return iode;
    }

    public void setPbias(double[] pbias,int i) {
        this.pbias[i] = pbias[i];
    }

    public double getPbias(int i) {
        return pbias[i];
    }

    public void setRefd(int refd) {
        this.refd = refd;
    }

    public int getRefd() {
        return refd;
    }

    public void setStdpb(float[] stdpb,int i) {
        this.stdpb[i] = stdpb[i];
    }

    public float getStdpb(int i) {
        return stdpb[i];
    }

    public void setUra(int ura) {
        this.ura = ura;
    }

    public int getUra() {
        return ura;
    }

    public void setYaw_ang(double yaw_ang) {
        this.yaw_ang = yaw_ang;
    }

    public double getYaw_ang() {
        return yaw_ang;
    }

    public void setYaw_rate(double yaw_rate) {
        this.yaw_rate = yaw_rate;
    }

    public double getYaw_rate() {
        return yaw_rate;
    }
}

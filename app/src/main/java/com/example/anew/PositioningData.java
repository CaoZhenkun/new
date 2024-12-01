package com.example.anew;

import android.hardware.SensorEvent;

import org.ejml.simple.SimpleMatrix;

import com.example.anew.Constellations.GpsTime;
import com.example.anew.Constellations.Time;
import com.example.anew.Satellites.*;
import com.example.anew.corrections.TopocentricCoordinates;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import Jama.Matrix;

/**
 * 室内定位算法类
 *@author QiWu
 */
public class PositioningData {

    //GNSS成员
    public Hashtable<Integer,EphData> ephDatahash=new Hashtable<Integer, EphData>(); //用于存储卫星星历数据的哈希表
    //public Hashtable<Integer,GNSSData> gnssDatahash=new Hashtable<Integer, GNSSData>(); //用于存储手机GNSS原始数据的哈希表
    public ArrayList<GNSSData> gnssDataArrayList= new ArrayList<>();//用于存储手机GNSS原始数据的数组
    private GpsTime epochTime;//历元时间
    public void setEpochTime(GpsTime epochTime) {this.epochTime = epochTime;}
    public GpsTime getEpochTime() {return epochTime;}

    public List<GNSSData> gpsDataList= new ArrayList<>();
    public List<GNSSData> qzssDataList= new ArrayList<>();
    public List<GNSSData> bdsDataList= new ArrayList<>();
    public List<GNSSData> galileoDataList= new ArrayList<>();
    public List<GNSSData> glonassDataList= new ArrayList<>();

    public List<GpsSatellite> gpsSatelliteList= new ArrayList<>();
    public List<QzssSatellite> qzssSatelliteList= new ArrayList<>();
    public List<BdsSatellite> bdsSatelliteList= new ArrayList<>();
    public List<GalileoSatellite> galileoSatelliteList= new ArrayList<>();
    public List<GlonassSatellite> glonassSatelliteList= new ArrayList<>();
    //public List<Satallites> satallitesList=new ArrayList<>();

    public int getSatelliteNum() {return gpsSatelliteList.size()+galileoSatelliteList.size()+glonassSatelliteList.size()+bdsSatelliteList.size()+qzssSatelliteList.size();}
    public Hashtable<Integer,SSRData> SSRDatahash=new Hashtable<Integer, SSRData>();//用于存储RTCM文件获得的SSR改正值
    public Hashtable<Time,IonoData> ionoDatahash=new Hashtable<Time, IonoData>();
    //public Hashtable<Integer,GNSSData> computDatahash=new Hashtable<Integer, GNSSData>(); //用于存储参与计算的最终数据的哈希表
    public Hashtable<String,GNSSData> computDatahash=new Hashtable<String, GNSSData>(); //用于存储参与计算的最终数据的哈希表
    public ArrayList<GNSSData> computDataList=new ArrayList<>();
    public Hashtable<String,GNSSData> doubleFreeDatahash=new Hashtable<String, GNSSData>();//用于存储双频无电离层组合数据哈希表

    public GNSSData gnssData=new GNSSData();

    public EphData ephData = new EphData();
    public SSRData ssrData = new SSRData();
    public IonoData ionoData = new IonoData();

    //Indoor成员
//    private final RawAccData rawAccData = new RawAccData();//当前时刻加速度数据类
    private final RawMagData magData = new RawMagData();//当前时刻磁场数据类
    private final WiFiData wiFiData = new WiFiData();//当前时刻WiFi数据类
    private List<MagRPData> magFingerprint = new ArrayList<>();//用于存储磁场指纹库
    private List<WiFiRPData> WiFiFingerprint = new ArrayList<>();//用于存储WiFi指纹库
    private final Indoor_Kalman_result indoorKalman_result = new Indoor_Kalman_result();//用于存储卡尔曼滤波结果

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
    }
    public class EphData {//卫星星历的数据存放类
        //都是一些星历参数
        private final static int STREAM_V = 1;
        private Time refTime; /* Reference time of the dataset */
        public Time getRefTime() {
            return refTime;
        }
        public void setRefTime(Time refTime) {
            this.refTime = refTime;
        }
        private char satType; /* Satellite Type */
        private int satID; /* Satellite ID number */
        private int week; /* GPS week number */
        private int L2Code; /* Code on L2 */
        private int L2Flag; /* L2 P data flag */
        private int svAccur; /* SV accuracy (URA index) */
        private int svHealth; /* SV health */
        private int iode; /* Issue of data (ephemeris) */
        private int iodc; /* Issue of data (clock) */
        private double toc; /* clock data reference time */
        private double toe; /* ephemeris reference time */
        private double tom; /* transmission time of message */
        /* satellite clock parameters */
        private double af0;
        private double af1;
        private double af2;
        private double tgd;
        /* satellite orbital parameters */
        private double rootA; /* Square root of the semimajor axis */
        private double e; /* Eccentricity */
        private double i0; /* Inclination angle at reference time */
        private double iDot; /* Rate of inclination angle */
        private double omg; /* Argument of perigee */
        private double omega0; /*
         * Longitude of ascending node of orbit plane at beginning
         * of week
         */
        private double omegaDot; /* Rate of right ascension */
        private double M0; /* Mean anomaly at reference time */
        private double deltaN; /* Mean motion difference from computed value */
        private double crc, crs, cuc, cus, cic, cis; /*
         * Amplitude of second-order harmonic
         * perturbations
         */
        private long fitInt; /* Fit interval */
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
        public int getWeek() {
            return week;
        }
        public void setWeek(int week) {
            this.week = week;
        }
        public int getL2Code() {
            return L2Code;
        }
        public void setL2Code(int l2Code) {
            L2Code = l2Code;
        }
        public int getL2Flag() {
            return L2Flag;
        }
        public void setL2Flag(int l2Flag) {
            L2Flag = l2Flag;
        }
        public int getSvAccur() {
            return svAccur;
        }
        public void setSvAccur(int svAccur) {
            this.svAccur = svAccur;
        }
        public int getSvHealth() {
            return svHealth;
        }
        public void setSvHealth(int svHealth) {
            this.svHealth = svHealth;
        }
        public int getIode() {
            return iode;
        }
        public void setIode(int iode) {
            this.iode = iode;
        }
        public int getIodc() {
            return iodc;
        }
        public void setIodc(int iodc) {
            this.iodc = iodc;
        }
        public double getToc() {
            return toc;
        }
        public void setToc(double toc) {
            this.toc = toc;
        }
        public double getToe() {
            return toe;
        }
        public void setToe(double toe) {
            this.toe = toe;
        }
        public double getTom() {
            return tom;
        }
        public void setTom(double tom) {
            this.tom = tom;
        }
        public double getAf0() {
            return af0;
        }
        public void setAf0(double af0) {
            this.af0 = af0;
        }
        public double getAf1() {
            return af1;
        }
        public void setAf1(double af1) {
            this.af1 = af1;
        }
        public double getAf2() {
            return af2;
        }
        public void setAf2(double af2) {
            this.af2 = af2;
        }
        public double getTgd() {
            return tgd;
        }
        public void setTgd(double tgd) {
            this.tgd = tgd;
        }
        public double getRootA() {
            return rootA;
        }
        public void setRootA(double rootA) {
            this.rootA = rootA;
        }
        public double getE() {
            return e;
        }
        public void setE(double e) {
            this.e = e;
        }
        public double getI0() {
            return i0;
        }
        public void setI0(double i0) {
            this.i0 = i0;
        }
        public double getiDot() {
            return iDot;
        }
        public void setiDot(double iDot) {
            this.iDot = iDot;
        }
        public double getOmg() {
            return omg;
        }
        public void setOmg(double omega) {
            this.omg = omega;
        }
        public double getOmega0() {
            return omega0;
        }
        public void setOmega0(double omega0) {
            this.omega0 = omega0;
        }
        public double getOmegaDot() {
            return omegaDot;
        }
        public void setOmegaDot(double omegaDot) {
            this.omegaDot = omegaDot;
        }
        public double getM0() {
            return M0;
        }
        public void setM0(double m0) {
            M0 = m0;
        }
        public double getDeltaN() {
            return deltaN;
        }
        public void setDeltaN(double deltaN) {
            this.deltaN = deltaN;
        }
        public double getCrc() {
            return crc;
        }
        public void setCrc(double crc) {
            this.crc = crc;
        }
        public double getCrs() {
            return crs;
        }
        public void setCrs(double crs) {
            this.crs = crs;
        }
        public double getCuc() {
            return cuc;
        }
        public void setCuc(double cuc) {
            this.cuc = cuc;
        }
        public double getCus() {
            return cus;
        }
        public void setCus(double cus) {
            this.cus = cus;
        }
        public double getCic() {
            return cic;
        }
        public void setCic(double cic) {
            this.cic = cic;
        }
        public double getCis() {
            return cis;
        }
        public void setCis(double cis) {
            this.cis = cis;
        }
        public long getFitInt() {
            return fitInt;
        }
        public void setFitInt(long fitInt) {
            this.fitInt = fitInt;
        }

    }
    public class SSRData {

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

        private double[] cbias = new double[68]; /* code biases (m) */

        private double[] pbias = new double[68]; /* phase biases (m) */

        private float[] stdpb = new float[68]; /* std-dev of phase biases (m) */

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
    public class IonoData {

        private Time ephTime;

        private double iod;//数据期龄：数据可用的起始时间与终止时间之差值。

        private double qual;//电离层质量指示

        private int layers;//电离层层数

        private double[] height=new double[layers];//电离层高度

        private int []degree=new int[layers];

        private int []order=new int[layers];

        private double[][] cosineC=new double[100][100];//余弦系数C

        private double[][] sineS=new double[100][100];//正弦系数S

        public void setEphTime(Time ephTime){
            this.ephTime=ephTime;
        }

        public Time getEphTime(){
            return ephTime;
        }

        public void setIod(double iod){
            this.iod=iod;
        }

        public double getIod(){
            return iod;
        }

        public void setQual(double qual){
            this.qual=qual;
        }

        public double getQual(){
            return qual;
        }

        public void setLayers(int layers){
            this.layers=layers;
        }

        public int getLayers(){
            return layers;
        }

        public void setHeight(double height,int i){
            this.height[i]=height;
        }

        public double getHeight(int i){
            return height[i];
        }

        public void setDegree(int degree,int i){
            this.degree[i]=degree;
        }

        public int getDegree(int i){
            return degree[i];
        }

        public void setOrder(int order,int i){
            this.order[i]=order;
        }

        public int getOrder(int i){ return order[i];}

        public void setCosineC(double[][] cosineC,int i,int j){
            this.cosineC[i][j]=cosineC[i][j];
        }
        public void setCosineC1(double cosineC,int i,int j){
            this.cosineC[i][j]=cosineC;
        }

        public double getCosineC(int i,int j){
            return cosineC[i][j];
        }

        public void setSineS(double[][] sineS,int i,int j){
            this.sineS[i][j]=sineS[i][j];
        }
        public void setSineS1(double sineS,int i,int j){
            this.sineS[i][j]=sineS;
        }

        public double getSineS(int i,int j){
            return sineS[i][j];
        }

    }

//    public static class RawAccData {
//        private float[] acc = new float[3];//包括三轴加速度信息与范数
//        private float[] gyr = new float[3];//包括三轴陀螺仪信息
//        private float acc_norm;//加速度范数
//        private float accmax;//同周期加速度最大值
//        private float accmin;//同周期加速度最小值
//        private double stepfre;//步频
//        private double curtime;//传感器时间
//        private float steplength;//步长
//        private float angle;//航向角
//        private double pdr_x=IndoorPosConstant.pdr_x;//PDR定位位置x坐标
//        private double pdr_y=IndoorPosConstant.pdr_y;//PDR定位位置y坐标
//
//        public void setAcc(SensorEvent event){
//            acc[0]=event.values[0];
//            acc[1]=event.values[1];
//            acc[2]=event.values[2];
//        }
//        public void setAcc_norm(float acc_norm) {
//            this.acc_norm = acc_norm;
//        }
//        public void setGyr(SensorEvent event){
//            gyr[0]=event.values[0];
//            gyr[1]=event.values[1];
//            gyr[2]=event.values[2];
//        }
//        public void setAccmax(float accmax) {
//            this.accmax = accmax;
//        }
//        public void setAccmin(float accmin) {
//            this.accmin = accmin;
//        }
//        public void setStepfre(double stepfre) {
//            this.stepfre = stepfre;
//        }
//        public void setCurtime(double curtime) {
//            this.curtime = curtime;
//        }
//        public void setSteplength(float steplength) {
//            this.steplength = steplength;
//        }
//        public void setAngle(float angle) {
//            this.angle = angle;
//        }
//        public void setPdr_x(double pdr_x) {
//            this.pdr_x = pdr_x;
//        }
//        public void setPdr_y(double pdr_y) {
//            this.pdr_y = pdr_y;
//        }
//        public float[] getAcc() {
//            return acc;
//        }
//        public float getSteplength() {
//            return steplength;
//        }
//        public float getAcc_norm() {
//            return acc_norm;
//        }
//        public double getStepfre() {
//            return stepfre;
//        }
//        public double getCurtime() {
//            return curtime;
//        }
//        public float getAngle() {
//            return angle;
//        }
//        public double getPdr_x() {
//            return pdr_x;
//        }
//        public double getPdr_y() {
//            return pdr_y;
//        }
//    }

    public static class MagRPData {
        private int RPID;//指纹点ID
        private float mag_norm;//磁场范数
        private double xpos;//x坐标
        private double ypos;//y坐标

        public void setRPMag(int values1,float values2,double values3,double values4){
            RPID = values1;
            mag_norm = values2;
            xpos = values3;
            ypos = values4;
        }
        public double getXpos() {
            return xpos;
        }
        public double getYpos() {
            return ypos;
        }
        public float getMag_norm() {
            return mag_norm;
        }
    }

    public static class RawMagData {
        private float[] mag = new float[3];//包括三轴磁场信息与范数
        private float mag_norm;//磁场模值
        private float[] magWeigh;//磁场相似度权重，未归一化
        private float[] distanceWeigh;//距离权重，未归一化
        private float[] allWeigh;//总权重，已归一化
        List<MagRPData> selectpoint;//磁场候选点序列
        private double[] magPos;//磁场匹配位置

        public void setMag(SensorEvent sensorEvent) {
            mag[0]=sensorEvent.values[0];
            mag[1]=sensorEvent.values[1];
            mag[2]=sensorEvent.values[2];
        }
        public void setMag_norm(float mag_norm) {
            this.mag_norm = mag_norm;
        }
        public void setMagWeigh(float[] magWeigh) {
            this.magWeigh = magWeigh;
        }
        public void setDistanceWeigh(float[] distanceWeigh) {
            this.distanceWeigh = distanceWeigh;
        }
        public void setAllWeigh(float[] allWeigh) {
            this.allWeigh = allWeigh;
        }
        public void setSelectpoint(List<MagRPData> selectpoint) {
            this.selectpoint = selectpoint;
        }
        public void setMagPos(double[] magPos) {
            this.magPos = magPos;
        }
        public float[] getMag() {
            return mag;
        }
        public float getMag_norm() {
            return mag_norm;
        }
        public double[] getMagPos() {
            return magPos;
        }
    }

    public static class WiFiData{
        private Hashtable<String, Integer> MAC_DB;//实时信号Hash表
        private double x;//WiFi匹配结果x坐标
        private double y;//WiFi匹配结果y坐标
        private double scanTime = -1;

        public void setX(double x) {
            this.x = x;
        }
        public void setY(double y) {
            this.y = y;
        }
        public void setScanTime(double scanTime) {
            this.scanTime = scanTime;
        }
        public double getScanTime() {
            return scanTime;
        }
        public Hashtable<String, Integer> getMAC_DB() {
            return MAC_DB;
        }
    }

    public static class WiFiRPData{
        private int id;//指纹点序号
        private double x;//指纹点x坐标
        private double y;//指纹点y坐标
        private Hashtable<String, Integer> MAC_DB;//基站MAC地址与对应信号强度

        public void setId(int id) {
            this.id = id;
        }
        public void setX(double x) {
            this.x = x;
        }
        public void setY(double y) {
            this.y = y;
        }
        public void setMAC_DB(Hashtable<String, Integer> MAC_DB) {
            this.MAC_DB = MAC_DB;
        }
        public int getId() {
            return id;
        }
        public double getX() {
            return x;
        }
        public double getY() {
            return y;
        }
        public Hashtable<String, Integer> getMAC_DB() {
            return MAC_DB;
        }
    }

    public static class Indoor_Kalman_result {
        private Matrix kalman_Pos;//用于存储最终滤波位置
        private Matrix kalman_P;//用于存储最终权重信息位置

        public Indoor_Kalman_result(){
            kalman_Pos = new Matrix(2,1,0);
            kalman_P = new Matrix(2,2,0);
            kalman_P.set(0,0,1);
            kalman_P.set(1,1,1);
        }
        public void setKalman_Pos(Matrix kalman_Pos) {
            this.kalman_Pos = kalman_Pos;
        }
        public void setKalman_P(Matrix kalman_P) {
            this.kalman_P = kalman_P;
        }
        public Matrix getKalman_Pos() {
            return kalman_Pos;
        }
        public Matrix getKalman_P() {
            return kalman_P;
        }
    }

//    public void setMagFingerprint(List<MagRPData> magFingerprint) {this.magFingerprint = magFingerprint;}
//    public void setWiFiFingerprint(List<WiFiRPData> wiFiFingerprint) {WiFiFingerprint = wiFiFingerprint;}
//    public List<MagRPData> getMagFingerprint() {
//        return magFingerprint;
//    }
//    public List<WiFiRPData> getWiFiFingerprint() {return WiFiFingerprint;}
//    public RawAccData getRawAccData(){
//        return rawAccData;
//    }
//    public RawMagData getRawMagData() {
//        return magData;
//    }
//    public WiFiData getWiFiData() {return wiFiData;}
//    public Indoor_Kalman_result getKalman_result() {
//        return indoorKalman_result;
//    }
//    public void fresh_pos(){
//        this.rawAccData.setPdr_x(indoorKalman_result.kalman_Pos.get(0,0));
//        this.rawAccData.setPdr_y(indoorKalman_result.kalman_Pos.get(1,0));
//    }
}

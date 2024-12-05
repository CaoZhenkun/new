package com.example.anew.Ntrip;


import android.util.Log;

import com.example.anew.Constants;
import com.example.anew.Constellations.Time;

import java.util.Date;

public class DecodeSSRData {
    public static class parameter{
        int i;
        int refd;
    }
    /* SSR signal and tracking mode IDs 确保数组为32------------------------------------------*/
    static final int[] ssr_sig_gps=new int[]{
            Constants.CODE_L1C,Constants.CODE_L1P,Constants.CODE_L1W,Constants.CODE_L1S,Constants.CODE_L1L,Constants.CODE_L2C,Constants.CODE_L2D, Constants.CODE_L2S,
            Constants.CODE_L2L,Constants.CODE_L2X,Constants.CODE_L2P,Constants.CODE_L2W,                 0,                 0,Constants.CODE_L5I,Constants.CODE_L5Q,
            0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0
    };
    static final int[] ssr_sig_glo=new int[]{
            Constants.CODE_L1C,Constants.CODE_L1P,Constants.CODE_L2C,Constants.CODE_L2P,Constants.CODE_L4A,Constants.CODE_L4B,Constants.CODE_L6A,Constants.CODE_L6B,
            Constants.CODE_L3I,Constants.CODE_L3Q,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0
    };
    static final int[] ssr_sig_gal=new int[]{
            Constants.CODE_L1A,Constants.CODE_L1B,Constants.CODE_L1C,                 0,                 0,Constants.CODE_L5I,Constants.CODE_L5Q,                 0,
            Constants.CODE_L7I,Constants.CODE_L7Q,                 0,Constants.CODE_L8I,Constants.CODE_L8Q,                 0,Constants.CODE_L6A,Constants.CODE_L6B,
            Constants.CODE_L6C,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0
    };
    static final int[] ssr_sig_qzs=new int[]{
            Constants.CODE_L1C,Constants.CODE_L1S,Constants.CODE_L1L,Constants.CODE_L2S,Constants.CODE_L2L,                  0,Constants.CODE_L5I,Constants.CODE_L5Q,
                             0,Constants.CODE_L6S,Constants.CODE_L6L,                 0,                 0,                  0,                 0,                 0,
                             0,Constants.CODE_L6E,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0
    };
    static final int[] ssr_sig_cmp=new int[]{
            Constants.CODE_L2I,Constants.CODE_L2Q,                 0,Constants.CODE_L6I,Constants.CODE_L6Q,                 0,Constants.CODE_L7I,Constants.CODE_L7Q,
                             0,Constants.CODE_L1D,Constants.CODE_L1P,                 0,Constants.CODE_L5D,Constants.CODE_L5P,                 0,Constants.CODE_L1A,
                             0,                 0,Constants.CODE_L6A,0,0,0,0,0,
            0,0,0,0,0,0,0,0
    };
    static final int[] ssr_sig_sbs=new int[]{
            Constants.CODE_L1C,Constants.CODE_L5I,Constants.CODE_L5Q,0,0,0,0,0,
            0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,
    };
    public static SSRGPS [] ssrgps = new SSRGPS[ConstantSystem.MAXSAT];//用于存储SSR改正信息的静态数组，数据接收时对其更新

    private static final String TAG = "decodeSSRData";

    private static Time time = new Time(0);

    /* SSR update intervals ------------------------------------------------------*/
    private static final double[] ssrudint ={
        1,2,5,10,15,30,60,120,240,300,600,900,1800,3600,7200,10800
    };
    private static final double gpst0[]={1980,1, 6,0,0,0}; /* gps time reference */
    private static final double gst0 []={1999,8,22,0,0,0}; /* galileo system time reference */
    private static final double bdt0 []={2006,1, 1,0,0,0}; /* beidou time reference */

    public static long getbitu(byte[] buff, int pos, int len) {
        long bits = 0L;
        int i;
        for (i = pos; i < pos + len; i++)
            bits = (bits << 1) + ((buff[i / 8] >> (7 - i % 8)) & 1L);
        return bits;
    }

    private static double getbitg(byte[] buff, int pos, int len) {
        double value = getbitu(buff, pos + 1, len - 1);
        return getbitu(buff, pos, 1) != 0 ? -value : value;
    }

    private static int getbits(byte[] buff, int pos, int len) {
        long  bits = getbitu(buff, pos, len);
        if (len <= 0 || 32 <= len || (bits & (1L << (len - 1))) == 0) return (int) bits;
        return (int) (bits | (~0L << len));
    }

    /* satellite system+prn/slot number to satellite number ------------------------
     * convert satellite system+prn/slot number to satellite number
     * args   : int    sys       I   satellite system (SYS_GPS,SYS_GLO,...)
     *          int    prn       I   satellite prn/slot number
     * return : satellite number (0:error)
     *-----------------------------------------------------------------------------*/
    public static int satno(int sys, int prn)
    {
        if (prn<=0) return 0;
        switch (sys) {
            case ConstantSystem.GPS_SYSTEM:
                if (prn<ConstantSystem.MINPRNGPS||ConstantSystem.MAXPRNGPS<prn) return 0;
                return prn-ConstantSystem.MINPRNGPS+1;
            case ConstantSystem.GLONASS_SYSTEM:
                if (prn<ConstantSystem.MINPRNGLO||ConstantSystem.MAXPRNGLO<prn) return 0;
                return ConstantSystem.NSATGPS+prn-ConstantSystem.MINPRNGLO+1;
            case ConstantSystem.GALILEO_SYSTEM:
                if (prn<ConstantSystem.MINPRNGAL||ConstantSystem.MAXPRNGAL<prn) return 0;
                return ConstantSystem.NSATGPS+ConstantSystem.NSATGLO+prn-ConstantSystem.MINPRNGAL+1;
            case ConstantSystem.QZSS_SYSTEM:
                if (prn<ConstantSystem.MINPRNQZS||ConstantSystem.MAXPRNQZS<prn) return 0;
                return ConstantSystem.NSATGPS+ConstantSystem.NSATGLO+ConstantSystem.NSATGAL+prn-ConstantSystem.MINPRNQZS+1;
            case ConstantSystem.BEIDOU_SYSTEM:
                if (prn<ConstantSystem.MINPRNCMP||ConstantSystem.MAXPRNCMP<prn) return 0;
                return ConstantSystem.NSATGPS+ConstantSystem.NSATGLO+ConstantSystem.NSATGAL+ConstantSystem.NSATQZS+prn-ConstantSystem.MINPRNCMP+1;
        }
        return 0;
    }

    /* convert calendar day/time to time -------------------------------------------
     * convert calendar day/time to gtime_t struct
     * args   : double *ep       I   day/time {year,month,day,hour,min,sec}
     * return : gtime_t struct
     * notes  : proper in 1970-2037 or 1970-2099 (64bit time_t)
     *-----------------------------------------------------------------------------*/
    public static double epoch2time(final double[] ep)
    {
    final int[] doy ={1,32,60,91,121,152,182,213,244,274,305,335};
        double time=0;
        int days,sec,year=(int)ep[0],mon=(int)ep[1],day=(int)ep[2];

        if (year<1970||2099<year||mon<1||12<mon) return time;

        /* leap year if year%4==0 in 1901-2099 */
        days=(year-1970)*365+(year-1969)/4+doy[mon-1]+day-2+(year%4==0&&mon>=3?1:0);
        //sec=(int)Math.floor(ep[5]);
        time=days*86400+(int)ep[3]*3600+(int)ep[4]*60+ep[5];
        return time;
    }

    /* gps time to time ------------------------------------------------------------
     * convert week and tow in gps time to gtime_t struct
     * args   : int    week      I   week number in gps time
     *          double sec       I   time of week in gps time (s)
     * return : gtime_t struct
     *-----------------------------------------------------------------------------*/
    public static double gpst2time(int week, double sec)
    {
        double t=epoch2time(gpst0);

        if (sec<-1E9||1E9<sec) sec=0.0;
        t+=86400*7*week+sec;
        return t*1E3;//返回毫秒级
    }

    /* adjust weekly rollover of GPS time ----------------------------------------*/
    static void adjweek(double tow)
    {
        double tow_p;
        Date date = new Date();

        /* if no time, get cpu time */
        if (time.getMsec()==0){
            time.setMsec((long)date.getTime());
            //time.setFraction(date.getTime()-(long)date.getTime());
        }
        int week = time.getGpsWeek();
        tow_p=time.getGpsWeekSec();
        if      (tow<tow_p-302400.0) tow+=604800.0;
        else if (tow>tow_p+302400.0) tow-=604800.0;
        time.setMsec((long)gpst2time(week,tow));
        time.setFraction(gpst2time(week,tow)-(long)gpst2time(week,tow));
    }


    /* decode SSR message epoch time ---------------------------------------------*/
    static int decode_ssr_epoch(byte[] data, int sys)
    {
        double tod,tow;
        int i=24+12;
        tow=getbitu(data,i,20); i+=20;
        adjweek(tow);
        return i;
    }

    /* decode SSR 1,4 message header ---------------------------------------------*/
    public static int decode_ssr1_head( byte[] data, int sys,double[] iod, double[] udi, parameter hsize)
    {
        int i=24+12,ns;
        long nsat,provid=0,solid=0;

        //ns=(sys==ConstantSystem.QZSS_SYSTEM)?4:6;
        ns=6;
        if (i+3+8+50+ns> data.length*8) return -1;
        i=decode_ssr_epoch(data,sys);
        long udint =getbitu(data,i, 4); i+= 4;
        int sync =(int)getbitu(data,i,1); i+=1;
        iod[0]=(int)getbitu(data,i, 4); i+= 4; /* IOD SSR */
        provid=getbitu(data,i,16); i+=16; /* provider ID */
        solid =getbitu(data,i, 4); i+= 4; /* solution ID */
        hsize.refd=(int)getbitu(data,i,1); i+=1; /* satellite ref datum */
        nsat  =getbitu(data,i,ns); i+=ns;
        udi[0]=ssrudint[(int)udint];
        hsize.i=i;
        return (int)nsat;
    }

    /* decode SSR 2,3,5,6 message header ---------------------------------------------*/
    public static int decode_ssr2_head(byte[] data, int sys, double[] iod, double[] udi, parameter hsize)
    {
        int i=24+12,ns;
        long nsat,provid=0,solid=0;

        ns=6;
        if (i+3+8+50+ns> data.length*8) return -1;
        i=decode_ssr_epoch(data,sys);
        int udint =(int)getbitu(data,i, 4); i+= 4;
        int sync =(int)getbitu(data,i,1); i+=1;
        iod[0]=(int)getbitu(data,i, 4); i+= 4; /* IOD SSR */
        provid=getbitu(data,i,16); i+=16; /* provider ID */
        solid =getbitu(data,i, 4); i+= 4; /* solution ID */
        nsat  =getbitu(data,i,ns); i+=ns;
        udi[0]=ssrudint[udint];
        hsize.i=i;
        return (int)nsat;
    }

    public static void decodeSSR1(byte[] data, int sys) {
        int type = (int)getbitu(data, 0, 12);
        Log.d("SSRTYPE", "-"+type);
        double[] udi = new double[6];
        double[] iod = new double[6];
        double[] deph = new double[3];
        double[] ddeph = new double[3];
        int j= 0,nsat= 0,sat= 0,np= 0,ni= 0,nj= 0,offp=0,sync=0;
        parameter mparameter = new parameter();
        mparameter.i=0;
        mparameter.refd=0;

        nsat=decode_ssr1_head(data,sys,iod,udi,mparameter);


        np=6; ni=8; nj=0;


        for (j=0;j<nsat&&mparameter.i+121+np+ni+nj<= data.length*8;j++) {

            int prn     =(int)getbitu(data,mparameter.i,np)+offp; mparameter.i+=np;
            int iode    =(int)getbitu(data,mparameter.i,ni);      mparameter.i+=ni;
            int iodcrc  =(int)getbitu(data,mparameter.i,nj);      mparameter.i+=nj;
            deph [0]=getbits(data,mparameter.i,22)*1E-4; mparameter.i+=22;
            deph [1]=getbits(data,mparameter.i,20)*4E-4; mparameter.i+=20;
            deph [2]=getbits(data,mparameter.i,20)*4E-4; mparameter.i+=20;
            ddeph[0]=getbits(data,mparameter.i,21)*1E-6; mparameter.i+=21;
            ddeph[1]=getbits(data,mparameter.i,19)*4E-6; mparameter.i+=19;
            ddeph[2]=getbits(data,mparameter.i,19)*4E-6; mparameter.i+=19;
            sat=satno(sys,prn);
            for (int i = 0; i < sat; i++) {
                if (ssrgps[i] == null) {
                    ssrgps[i] = new SSRGPS();
                }
            }
            switch (type) {
                case 1057:
                    ssrgps[sat-1].setSatType(ConstantSystem.GPS_SYSTEM);
                    break;
                case 1063:
                    ssrgps[sat-1].setSatType(ConstantSystem.GLONASS_SYSTEM);
                    break;
                case 1240:
                    ssrgps[sat-1].setSatType(ConstantSystem.GALILEO_SYSTEM);
                    break;
                case 1246:
                    ssrgps[sat-1].setSatType(ConstantSystem.QZSS_SYSTEM);
                    break;
                case 1252:
                    ssrgps[sat-1].setSatType(ConstantSystem.SBAS_SYSTEM);
                    break;
                case 1258:
                    ssrgps[sat-1].setSatType(ConstantSystem.BEIDOU_SYSTEM);
                    break;
            }
            ssrgps[sat-1].setEphTime(time);
            ssrgps[sat-1].setUdi(udi,0);
            ssrgps[sat-1].setIod(iod,0);
            ssrgps[sat-1].setIode(iode);           /* SBAS/BDS: toe/t0 modulo */
            ssrgps[sat-1].setIodcrc(iodcrc);           /* SBAS/BDS: IOD CRC */
            ssrgps[sat-1].setRefd(mparameter.refd);
            for (int k=0;k<3;k++) {
                ssrgps[sat - 1].setDeph(deph, k);
                ssrgps[sat - 1].setDdeph(ddeph, k);
            }
            ssrgps[sat-1].setSatID(sat);

        }
        System.out.println("h");
    }

    public static void decodeSSR2(byte[] data,int sys) {
        int type = (int)getbitu(data, 0, 12);
        Log.d("SSRTYPE", "-"+type);
        double[] udi = new double[6];
        double[] iod = new double[6];
        double[] dclk = new double[3];
        int j= 0,nsat= 0,sat= 0,np= 0,offp=0;
        parameter mparameter = new parameter();
        mparameter.i=0;

        nsat=decode_ssr2_head(data,sys,iod,udi,mparameter);



        np=6;


        for (j=0;j<nsat&&mparameter.i+5+np<= data.length*8;j++) {
            int prn=(int)getbitu(data,mparameter.i,np)+offp; mparameter.i+=np;
            dclk[0]=getbits(data,mparameter.i,22)*1E-4; mparameter.i+=22;
            dclk[1]=getbits(data,mparameter.i,21)*1E-6; mparameter.i+=21;
            dclk[2]=getbits(data,mparameter.i,27)*2E-8; mparameter.i+=27;
            sat=satno(sys,prn);
            for (int i = 0; i < sat; i++) {
                if (ssrgps[i] == null) {
                    ssrgps[i] = new SSRGPS();
                }
            }

            switch (type) {
                case 1057:
                    ssrgps[sat-1].setSatType(ConstantSystem.GPS_SYSTEM);
                    break;
                case 1063:
                    ssrgps[sat-1].setSatType(ConstantSystem.GLONASS_SYSTEM);
                    break;
                case 1240:
                    ssrgps[sat-1].setSatType(ConstantSystem.GALILEO_SYSTEM);
                    break;
                case 1246:
                    ssrgps[sat-1].setSatType(ConstantSystem.QZSS_SYSTEM);
                    break;
                case 1252:
                    ssrgps[sat-1].setSatType(ConstantSystem.SBAS_SYSTEM);
                    break;
                case 1258:
                    ssrgps[sat-1].setSatType(ConstantSystem.BEIDOU_SYSTEM);
                    break;
            }
            ssrgps[sat-1].setClkTime(time);
            ssrgps[sat-1].setUdi(udi,1);
            ssrgps[sat-1].setIod(iod,1);
            for (int k=0;k<3;k++) {
                ssrgps[sat-1].setDclk(dclk,k);
            }
            ssrgps[sat-1].setSatID(sat);

        }
        System.out.println("h");
    }

    public static void decodeSSR3(byte[] data,int sys) {
        int type = (int)getbitu(data, 0, 12);
        Log.d("SSRTYPE", "-"+type);
        double[] udi = new double[6];
        double[] iod = new double[6];
        double[] cbias = new double[Constants.MAXCODE];
        int[] sigs = new int[32];
        double bias=0.0;
        int j= 0,nsat= 0,sat= 0,np= 0,offp=0,mode=0;
        parameter mparameter = new parameter();
        mparameter.i=0;
        nsat=decode_ssr2_head(data,sys,iod,udi,mparameter);
        np=6;
        for (j=0;j<nsat&&mparameter.i+70+np<= data.length*8;j++) {
            int prn=(int)getbitu(data,mparameter.i,np)+offp; mparameter.i+=np;
            int nbias=(int)getbitu(data,mparameter.i,5); mparameter.i+= 5;
            for (int k=0;k<Constants.MAXCODE;k++) cbias[k]=0;
            for (int k=0;k<nbias&&mparameter.i+19<= data.length*8;k++) {
                mode =(int)getbitu(data,mparameter.i,5);
                mparameter.i+= 5;
                bias = getbits(data,mparameter.i,14) * 0.01;
                mparameter.i += 14;
                if (sigs[mode]!=0) {
                    cbias[sigs[mode] - 1] = (float) bias;
                }
            }
            sat=satno(sys,prn);
            for (int i = 0; i < sat; i++) {
                if (ssrgps[i] == null) {
                    ssrgps[i] = new SSRGPS();
                }
            }
            switch (type) {
                case 1057:
                    ssrgps[sat-1].setSatType(ConstantSystem.GPS_SYSTEM);
                    break;
                case 1063:
                    ssrgps[sat-1].setSatType(ConstantSystem.GLONASS_SYSTEM);
                    break;
                case 1240:
                    ssrgps[sat-1].setSatType(ConstantSystem.GALILEO_SYSTEM);
                    break;
                case 1246:
                    ssrgps[sat-1].setSatType(ConstantSystem.QZSS_SYSTEM);
                    break;
                case 1252:
                    ssrgps[sat-1].setSatType(ConstantSystem.SBAS_SYSTEM);
                    break;
                case 1258:
                    ssrgps[sat-1].setSatType(ConstantSystem.BEIDOU_SYSTEM);
                    break;
            }
            ssrgps[sat-1].setBiasTime(time);
            ssrgps[sat-1].setUdi(udi,4);
            ssrgps[sat-1].setIod(iod,4);
            for (int k=0;k<Constants.MAXCODE;k++) {
                ssrgps[sat-1].setCbias(cbias,k);
            }
            ssrgps[sat-1].setSatID(sat);

        }
        System.out.println("h");

    }

//    public static void decodeSSR4(byte[] data,int sys,int subtype) {
//        int type = (int)getbitu(data, 0, 12);
//        Log.d("SSRTYPE", "-"+type);
//        double[] udi = new double[6];
//        double[] iod = new double[6];
//        double[] deph = new double[3];
//        double[] ddeph = new double[3];
//        double[] dclk = new double[3];
//        int j= 0,nsat= 0,sat= 0,np= 0,ni= 0,nj= 0,offp=0;
//        parameter mparameter = new parameter();
//        mparameter.i=0;
//        mparameter.refd=0;
//
//        nsat=decode_ssr1_head(data,sys,subtype,iod,udi,mparameter);
//        if (subtype>0) { /* IGS SSR */
//            np=6;ni=8;nj=0;
//            //if      (sys==SYS_CMP) offp=0;
//            //else if (sys==SYS_SBS) offp=119;
//        }
//
//        for (j=0;j<nsat&&mparameter.i+191+np+ni+nj<= data.length*8;j++) {
//
//            int prn     =(int)getbitu(data,mparameter.i,np)+offp; mparameter.i+=np;
//            int iode    =(int)getbitu(data,mparameter.i,ni);      mparameter.i+=ni;
//            int iodcrc  =(int)getbitu(data,mparameter.i,nj);      mparameter.i+=nj;
//            deph [0]=getbits(data,mparameter.i,22)*1E-4; mparameter.i+=22;
//            deph [1]=getbits(data,mparameter.i,20)*4E-4; mparameter.i+=20;
//            deph [2]=getbits(data,mparameter.i,20)*4E-4; mparameter.i+=20;
//            ddeph[0]=getbits(data,mparameter.i,21)*1E-6; mparameter.i+=21;
//            ddeph[1]=getbits(data,mparameter.i,19)*4E-6; mparameter.i+=19;
//            ddeph[2]=getbits(data,mparameter.i,19)*4E-6; mparameter.i+=19;
//            dclk[0]=getbits(data,mparameter.i,22)*1E-4; mparameter.i+=22;
//            dclk[1]=getbits(data,mparameter.i,21)*1E-6; mparameter.i+=21;
//            dclk[2]=getbits(data,mparameter.i,27)*2E-8; mparameter.i+=27;
//            sat=satno(sys,prn);
//            for (int i = 0; i < sat; i++) {
//                if (ssrgps[i] == null) {
//                    ssrgps[i] = new SSRGPS();
//                }
//            }
//            switch (type) {
//                case 1057:
//                    ssrgps[sat-1].setSatType(ConstantSystem.GPS_SYSTEM);
//                    break;
//                case 1063:
//                    ssrgps[sat-1].setSatType(ConstantSystem.GLONASS_SYSTEM);
//                    break;
//                case 1240:
//                    ssrgps[sat-1].setSatType(ConstantSystem.GALILEO_SYSTEM);
//                    break;
//                case 1246:
//                    ssrgps[sat-1].setSatType(ConstantSystem.QZSS_SYSTEM);
//                    break;
//                case 1252:
//                    ssrgps[sat-1].setSatType(ConstantSystem.SBAS_SYSTEM);
//                    break;
//                case 1258:
//                    ssrgps[sat-1].setSatType(ConstantSystem.BEIDOU_SYSTEM);
//                    break;
//            }
//            ssrgps[sat-1].setEphTime(time);
//            ssrgps[sat-1].setClkTime(time);
//            ssrgps[sat-1].setUdi(udi,0);
//            ssrgps[sat-1].setIod(iod,0);
//            ssrgps[sat-1].setUdi(udi,1);
//            ssrgps[sat-1].setIod(iod,1);
//            ssrgps[sat-1].setIode(iode);           /* SBAS/BDS: toe/t0 modulo */
//            ssrgps[sat-1].setIodcrc(iodcrc);           /* SBAS/BDS: IOD CRC */
//            ssrgps[sat-1].setRefd(mparameter.refd);
//            for (int k=0;k<3;k++) {
//                ssrgps[sat - 1].setDeph(deph, k);
//                ssrgps[sat - 1].setDdeph(ddeph, k);
//                ssrgps[sat - 1].setDclk(dclk, k);
//            }
//            ssrgps[sat-1].setSatID(sat);
//        }
//
//    }
//
//    public static void decodeSSR5(byte[] data,int sys,int subtype) {
//        int type = (int)getbitu(data, 0, 12);
//        Log.d("SSRTYPE", "-"+type);
//        double[] udi = new double[6];
//        double[] iod = new double[6];
//        int j= 0,nsat= 0,sat= 0,np= 0,offp=0;
//        parameter mparameter = new parameter();
//        mparameter.i=0;
//
//        nsat=decode_ssr2_head(data,sys,subtype,iod,udi,mparameter);
//
//        if (subtype>0) { /* IGS SSR */
//            np=6;
//            //if      (sys==SYS_CMP) offp=0;
//            //else if (sys==SYS_SBS) offp=119;
//        }
//        for (j=0;j<nsat&&mparameter.i+6+np<= data.length*8;j++) {
//            int prn=(int)getbitu(data,mparameter.i,np)+offp; mparameter.i+=np;
//            int ura=(int)getbitu(data,mparameter.i,6);   mparameter.i+= 6;
//
//            sat=satno(sys,prn);
//            for (int i = 0; i < sat; i++) {
//                if (ssrgps[i] == null) {
//                    ssrgps[i] = new SSRGPS();
//                }
//            }
//
//            switch (type) {
//                case 1057:
//                    ssrgps[sat-1].setSatType(ConstantSystem.GPS_SYSTEM);
//                    break;
//                case 1063:
//                    ssrgps[sat-1].setSatType(ConstantSystem.GLONASS_SYSTEM);
//                    break;
//                case 1240:
//                    ssrgps[sat-1].setSatType(ConstantSystem.GALILEO_SYSTEM);
//                    break;
//                case 1246:
//                    ssrgps[sat-1].setSatType(ConstantSystem.QZSS_SYSTEM);
//                    break;
//                case 1252:
//                    ssrgps[sat-1].setSatType(ConstantSystem.SBAS_SYSTEM);
//                    break;
//                case 1258:
//                    ssrgps[sat-1].setSatType(ConstantSystem.BEIDOU_SYSTEM);
//                    break;
//            }
//            ssrgps[sat-1].setUraTime(time);
//            ssrgps[sat-1].setUdi(udi,3);
//            ssrgps[sat-1].setIod(iod,3);
//            ssrgps[sat-1].setUra(ura);
//            ssrgps[sat-1].setSatID(sat);
//
//        }
//
//    }

//    public static void decodeSSR6(byte[] data,int sys,int subtype) {
//        int type = (int)getbitu(data, 0, 12);
//        Log.d("SSRTYPE", "-"+type);
//        double[] udi = new double[6];
//        double[] iod = new double[6];
//        int j= 0,nsat= 0,sat= 0,np= 0,offp=0;
//        parameter mparameter = new parameter();
//        mparameter.i=0;
//
//        nsat=decode_ssr2_head(data,sys,subtype,iod,udi,mparameter);
//
//        if (subtype>0) { /* IGS SSR */
//            np=6;
//            //if      (sys==SYS_CMP) offp=0;
//            //else if (sys==SYS_SBS) offp=119;
//        }
//
//        for (j=0;j<nsat&&mparameter.i+22+np<= data.length*8;j++) {
//            int prn=(int)getbitu(data,mparameter.i,np)+offp; mparameter.i+=np;
//            double hrclk=getbits(data,mparameter.i,22)*1E-4; mparameter.i+=22;
//
//            sat=satno(sys,prn);
//            for (int i = 0; i < sat; i++) {
//                if (ssrgps[i] == null) {
//                    ssrgps[i] = new SSRGPS();
//                }
//            }
//
//            switch (type) {
//                case 1057:
//                    ssrgps[sat-1].setSatType(ConstantSystem.GPS_SYSTEM);
//                    break;
//                case 1063:
//                    ssrgps[sat-1].setSatType(ConstantSystem.GLONASS_SYSTEM);
//                    break;
//                case 1240:
//                    ssrgps[sat-1].setSatType(ConstantSystem.GALILEO_SYSTEM);
//                    break;
//                case 1246:
//                    ssrgps[sat-1].setSatType(ConstantSystem.QZSS_SYSTEM);
//                    break;
//                case 1252:
//                    ssrgps[sat-1].setSatType(ConstantSystem.SBAS_SYSTEM);
//                    break;
//                case 1258:
//                    ssrgps[sat-1].setSatType(ConstantSystem.BEIDOU_SYSTEM);
//                    break;
//            }
//            ssrgps[sat-1].setHrclkTime(time);
//            ssrgps[sat-1].setUdi(udi,2);
//            ssrgps[sat-1].setIod(iod,2);
//            ssrgps[sat-1].setHrclk(hrclk);
//            ssrgps[sat-1].setSatID(sat);
//        }
//    }


    public static void decodessr(byte[] data) {


        int type = (int)getbitu(data, 0, 12);


        switch (type) {
            case 1057:
                 decodeSSR1(data,ConstantSystem.GPS_SYSTEM);
            case 1058:
                 decodeSSR2(data,ConstantSystem.GPS_SYSTEM);
            case 1059:
                 decodeSSR3(data,ConstantSystem.GPS_SYSTEM);
        }

    }
}

/*
 * Copyright (c) 2010, Eugenio Realini, Mirko Reguzzoni, Cryms sagl - Switzerland. All Rights Reserved.
 *
 * This file is part of goGPS Project (goGPS).
 *
 * goGPS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * goGPS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with goGPS.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package com.example.anew.navifromftp;

import com.example.anew.Constellations.Time;

/**
 * <p>
 * GPS broadcast ephemerides
 * </p>
 *
 * @author Eugenio Realini, Cryms.com, Daisuke Yoshida 
 */

public class EphGps {
	private final static int STREAM_V = 1;

	private Time refTime; /* Reference time of the dataset */
	private char satType; /* Satellite Type *///G C E R。。。。。。
	private int satID; /* Satellite ID number *///卫星id第一行第0个
	private int week; /* GPS week number */// GPS时间周第六行第三个

	private int L2Code; /* Code on L2 */// L2频道C/A码标识第六行第二个
	private int L2Flag; /* L2 P data flag *///L2P码标识第六行第四个

	private int svAccur; /* SV accuracy (URA index) *///卫星精度第七行第一个
	private int svHealth; /* SV health *///卫星健康第七行第二个

	private int iode; /* Issue of data (ephemeris) *///数据龄期第二行第一个
	private int iodc; /* Issue of data (clock) *///星钟的数据质量第七行第四个

	private double toc; /* clock data reference time *///卫星钟时间第一行第一个
	private double toe; /* ephemeris reference time *///星历的参考时刻 第四行第一个
	private double tom; /* transmission time of message *///信息发射时间第八行第一个

	/* satellite clock parameters */
	private double af0;//卫星钟差第一行第二个
	private double af1;//卫星钟偏第一行第三个
	private double af2;//卫星钟偏移第一行第四个
	private double tgd;//电离层延迟第七行第三个

	/* satellite orbital parameters */
	private double rootA; /* Square root of the semimajor axis *///轨道长半轴平方根 第三行第四个
	private double e; /* Eccentricity */// 轨道偏心率 第三行第二个
	private double i0; /* Inclination angle at reference time *///轨道倾角第五行第一个
	private double iDot; /* Rate of inclination angle *///轨道倾角的变率第六行第一个
	private double omega; /* Argument of perigee *///近地点角距第五行第三个
	private double omega0; //升交点经度第四行第三个
	/*//
	 * Longitude of ascending node of orbit plane at beginning
	 * of week
	 */
	private double omegaDot; /* Rate of right ascension *///升交点赤经变化第五行第四个
	private double M0; /* Mean anomaly at reference time *///M0平近点角 第二行第四个
	private double deltaN; /* Mean motion difference from computed value *///deltaN平均角速度改正项 第二行第三个


	//crc轨道半径的改正项第五行第二个
	//crs轨道半径改正项 第二行第二个
	//cuc升交点角距改正项 第三行第一个
	//cus升交点角距改正项 第三行第三个
	//cic 轨道倾角的改正项 第四行第二个
	//cis轨道倾角改正项第四行第四个
	private double crc, crs, cuc, cus, cic, cis; /*

	 * Amplitude of second-order harmonic
	 * perturbations
	 */
	private long fitInt; /* Fit interval *///拟合区间第八行第二个

	/* for GLONASS data */
	private float tow;

	private float tauN;//钟偏差
	private float gammaN;//相对频率偏差
	private double tk;//电文帧时间

	private double X;
	private double Xv;
	private double Xa;
	private double Bn;//健康状态 0健康 1不健康

	private double Y;
	private double Yv;
	private double Ya;
	private int freq_num;//卫星频率数 1-24颗卫星

	private double Z;
	private double Zv;
	private double Za;
	private double En;//运行年龄

    public static final EphGps UnhealthyEph = new EphGps();


	public EphGps(){

	}


    /**
	 * @return the refTime
	 */
	public Time getRefTime() {
		return refTime;
	}
	/**
	 * @param refTime the refTime to set
	 */
	public void setRefTime(Time refTime) {
		this.refTime = refTime;
	}
	/**
	 * @return the satType
	 */
	public char getSatType() {
		return satType;
	}
	/**
	 * @param satType the satType to set
	 */
	public void setSatType(char satType) {
		this.satType = satType;
	}
	/**
	 * @return the satID
	 */
	public int getSatID() {
		return satID;
	}
	/**
	 * @param satID the satID to set
	 */
	public void setSatID(int satID) {
		this.satID = satID;
	}
	/**
	 * @return the week
	 */
	public int getWeek() {
		return week;
	}
	/**
	 * @param week the week to set
	 */
	public void setWeek(int week) {
		this.week = week;
	}
	/**
	 * @return the l2Code
	 */
	public int getL2Code() {
		return L2Code;
	}
	/**
	 * @param l2Code the l2Code to set
	 */
	public void setL2Code(int l2Code) {
		L2Code = l2Code;
	}
	/**
	 * @return the l2Flag
	 */
	public int getL2Flag() {
		return L2Flag;
	}
	/**
	 * @param l2Flag the l2Flag to set
	 */
	public void setL2Flag(int l2Flag) {
		L2Flag = l2Flag;
	}
	/**
	 * @return the svAccur
	 */
	public int getSvAccur() {
		return svAccur;
	}
	/**
	 * @param svAccur the svAccur to set
	 */
	public void setSvAccur(int svAccur) {
		this.svAccur = svAccur;
	}
	/**
	 * @return the svHealth
	 */
	public int getSvHealth() {
		return svHealth;
	}
	/**
	 * @param svHealth the svHealth to set
	 */
	public void setSvHealth(int svHealth) {
		this.svHealth = svHealth;
	}
	/**
	 * @return the iode
	 */
	public int getIode() {
		return iode;
	}
	/**
	 * @param iode the iode to set
	 */
	public void setIode(int iode) {
		this.iode = iode;
	}
	/**
	 * @return the iodc
	 */
	public int getIodc() {
		return iodc;
	}
	/**
	 * @param iodc the iodc to set
	 */
	public void setIodc(int iodc) {
		this.iodc = iodc;
	}
	/**
	 * @return the toc
	 */
	public double getToc() {
		return toc;
	}
	/**
	 * @param toc the toc to set
	 */
	public void setToc(double toc) {
		this.toc = toc;
	}
	/**
	 * @return the toe
	 */
	public double getToe() {
		return toe;
	}
	/**
	 * @param toe the toe to set
	 */
	public void setToe(double toe) {
		this.toe = toe;
	}
	/**
	 * @return the tom
	 */
	public double getTom() {
		return tom;
	}
	/**
	 * @param tom the tom to set
	 */
	public void setTom(double tom) {
		this.tom = tom;
	}
	/**
	 * @return the af0
	 */
	public double getAf0() {
		return af0;
	}
	/**
	 * @param af0 the af0 to set
	 */
	public void setAf0(double af0) {
		this.af0 = af0;
	}
	/**
	 * @return the af1
	 */
	public double getAf1() {
		return af1;
	}
	/**
	 * @param af1 the af1 to set
	 */
	public void setAf1(double af1) {
		this.af1 = af1;
	}
	/**
	 * @return the af2
	 */
	public double getAf2() {
		return af2;
	}
	/**
	 * @param af2 the af2 to set
	 */
	public void setAf2(double af2) {
		this.af2 = af2;
	}
	/**
	 * @return the tgd
	 */
	public double getTgd() {
		return tgd;
	}
	/**
	 * @param tgd the tgd to set
	 */
	public void setTgd(double tgd) {
		this.tgd = tgd;
	}
	/**
	 * @return the rootA
	 */
	public double getRootA() {
		return rootA;
	}
	/**
	 * @param rootA the rootA to set
	 */
	public void setRootA(double rootA) {
		this.rootA = rootA;
	}
	/**
	 * @return the e
	 */
	public double getE() {
		return e;
	}
	/**
	 * @param e the e to set
	 */
	public void setE(double e) {
		this.e = e;
	}
	/**
	 * @return the i0
	 */
	public double getI0() {
		return i0;
	}
	/**
	 * @param i0 the i0 to set
	 */
	public void setI0(double i0) {
		this.i0 = i0;
	}
	/**
	 * @return the iDot
	 */
	public double getiDot() {
		return iDot;
	}
	/**
	 * @param iDot the iDot to set
	 */
	public void setiDot(double iDot) {
		this.iDot = iDot;
	}
	/**
	 * @return the omega
	 */
	public double getOmega() {
		return omega;
	}
	/**
	 * @param omega the omega to set
	 */
	public void setOmega(double omega) {
		this.omega = omega;
	}
	/**
	 * @return the omega0
	 */
	public double getOmega0() {
		return omega0;
	}
	/**
	 * @param omega0 the omega0 to set
	 */
	public void setOmega0(double omega0) {
		this.omega0 = omega0;
	}
	/**
	 * @return the omegaDot
	 */
	public double getOmegaDot() {
		return omegaDot;
	}
	/**
	 * @param omegaDot the omegaDot to set
	 */
	public void setOmegaDot(double omegaDot) {
		this.omegaDot = omegaDot;
	}
	/**
	 * @return the m0
	 */
	public double getM0() {
		return M0;
	}
	/**
	 * @param m0 the m0 to set
	 */
	public void setM0(double m0) {
		M0 = m0;
	}
	/**
	 * @return the deltaN
	 */
	public double getDeltaN() {
		return deltaN;
	}
	/**
	 * @param deltaN the deltaN to set
	 */
	public void setDeltaN(double deltaN) {
		this.deltaN = deltaN;
	}
	/**
	 * @return the crc
	 */
	public double getCrc() {
		return crc;
	}
	/**
	 * @param crc the crc to set
	 */
	public void setCrc(double crc) {
		this.crc = crc;
	}
	/**
	 * @return the crs
	 */
	public double getCrs() {
		return crs;
	}
	/**
	 * @param crs the crs to set
	 */
	public void setCrs(double crs) {
		this.crs = crs;
	}
	/**
	 * @return the cuc
	 */
	public double getCuc() {
		return cuc;
	}
	/**
	 * @param cuc the cuc to set
	 */
	public void setCuc(double cuc) {
		this.cuc = cuc;
	}
	/**
	 * @return the cus
	 */
	public double getCus() {
		return cus;
	}
	/**
	 * @param cus the cus to set
	 */
	public void setCus(double cus) {
		this.cus = cus;
	}
	/**
	 * @return the cic
	 */
	public double getCic() {
		return cic;
	}
	/**
	 * @param cic the cic to set
	 */
	public void setCic(double cic) {
		this.cic = cic;
	}
	/**
	 * @return the cis
	 */
	public double getCis() {
		return cis;
	}
	/**
	 * @param cis the cis to set
	 */
	public void setCis(double cis) {
		this.cis = cis;
	}
	/**
	 * @return the fitInt
	 */
	public long getFitInt() {
		return fitInt;
	}
	/**
	 * @param fitInt the fitInt to set
	 */
	public void setFitInt(long fitInt) {
		this.fitInt = fitInt;
	}


	/* for GLONASS data */

	public float getTauN() {
		return tauN;
	}
	public void setTauN(float tauN) {
		this.tauN = tauN;
	}

	public float getGammaN() {
		return gammaN;
	}
	public void setGammaN(float gammaN) {
		this.gammaN = gammaN;
	}

	public double gettk() {
		return tk;
	}
	public void settk(double tk) {
		this.tk = tk;
	}

	public double getX() {
		return X;
	}
	public void setX(double X) {
		this.X = X;
	}

	public double getXv() {
		return Xv;
	}
	public void setXv(double Xv) {
		this.Xv = Xv;
	}

	public double getXa() {
		return Xa;
	}
	public void setXa(double Xa) {
		this.Xa = Xa;
	}

	public double getBn() {
		return Bn;
	}
	public void setBn(double Bn) {
		this.Bn = Bn;
	}

	public double getY() {
		return Y;
	}
	public void setY(double Y) {
		this.Y = Y;
	}

	public double getYv() {
		return Yv;
	}
	public void setYv(double Yv) {
		this.Yv = Yv;
	}

	public double getYa() {
		return Ya;
	}
	public void setYa(double Ya) {
		this.Ya = Ya;
	}

	public int getfreq_num() {
		return freq_num;
	}
	public void setfreq_num(int freq_num) {
		this.freq_num = freq_num;
	}

	public double getZ() {
		return Z;
	}
	public void setZ(double Z) {
		this.Z = Z;
	}

	public double getZv() {
		return Zv;
	}
	public void setZv(double Zv) {
		this.Zv = Zv;
	}

	public double getZa() {
		return Za;
	}
	public void setZa(double Za) {
		this.Za = Za;
	}

	public double getEn() {
		return En;
	}
	public void setEn(double En) {
		this.En = En;
	}



}

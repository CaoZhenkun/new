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
package com.example.anew.coord;



import com.example.anew.Constellations.Time;
import com.example.anew.GNSSConstants;

import org.ejml.simple.SimpleMatrix;

/**
 * <p>
 * Coordinate and reference system tools
 * </p>
 *
 * @author Eugenio Realini, Cryms.com
 */
public class Coordinates {
	private final static int STREAM_V = 1;

	// Global systems
	private SimpleMatrix ecef = null; /* Earth-Centered, Earth-Fixed (X, Y, Z) */
	private SimpleMatrix geod = null; /* Longitude (lam), latitude (phi), height (h) */

	// Local systems (require to specify an origin)
	private SimpleMatrix enu; /* Local coordinates (East, North, Up) */

	private Time refTime = null;

	protected Coordinates(){
		ecef = new SimpleMatrix(3, 1);
		geod = new SimpleMatrix(3, 1);
		enu = new SimpleMatrix(3, 1);
	}


	public static Coordinates globalXYZInstance(double x, double y, double z){
		Coordinates c = new Coordinates();
		//c.ecef = new SimpleMatrix(3, 1);
		c.setXYZ(x, y, z);
		c.computeGeodetic();
		return c;
	}
//	public static Coordinates globalXYZInstance(SimpleMatrix ecef){
//		Coordinates c = new Coordinates();
//		c.ecef = ecef.copy();
//		return c;
//	}
	public static Coordinates globalENUInstance(SimpleMatrix ecef){
		Coordinates c = new Coordinates();
		c.enu = ecef.copy();
		return c;
	}


	/**
	 Converts the differences between two sets of coordinates from Latitude, Longitude to
	 North and East.

	 The considered ellipsoid is WGS-84

	 @param lat      =  the latitude of the reference position [radians]
	 @param height   =  the height on the ellipsoid of the reference position [m]
	 @param deltaLat =  differences in latitude [radians]
	 @param deltaLon =  differences in longitude [radians]

	 */
	 public static double[] deltaGeodeticToDeltaMeters(double lat, double height, double deltaLat, double deltaLon){

    	 // Declare the required WGS-84 ellipsoid parameters
		 final double a = 6378137.0;     // Semi-major axis
		 final double b = 6356752.31425; // Semi-minor axis

		 // Compute the second eccentricity
		 final double e2 = (Math.pow(a,2) - Math.pow(b,2)) / Math.pow(a,2);

		 // Compute additional parameter required in the processing
		 final double W = Math.sqrt(1.0 - e2 *(Math.pow(Math.sin(lat), 2)));

		 // Compute the meridian radius of curvature at the given latitude
         final double M = (a * (1.0 - e2)) / Math.pow(W, 3);

         // Compute the the prime vertical radius of curvature at the given latitude
		 final double N = a / W;

		 // Compute the differences on North and East
		 double deltaN = deltaLat * (M + height);
		 double deltaE = deltaLon * (N + height) * Math.cos(lat);

		 return new double[]{deltaN, deltaE};
	}


	public static Coordinates globalGeodInstance( double lat, double lon, double alt ){
		Coordinates c = new Coordinates();
		//c.ecef = new SimpleMatrix(3, 1);
		c.setGeod( lat, lon, alt);
		c.computeECEF();

		if( !c.isValidXYZ() )
			throw new RuntimeException("Invalid ECEF: " + c);
		return c;
	}

	public SimpleMatrix minusXYZ(Coordinates coord){
		return this.ecef.minus(coord.ecef);
	}
	/**
	 *
	 */
	public void computeGeodetic() {
		double X = this.ecef.get(0);
		double Y = this.ecef.get(1);
		double Z = this.ecef.get(2);

		//this.geod = new SimpleMatrix(3, 1);

		double a = GNSSConstants.WGS84_SEMI_MAJOR_AXIS;
		double e = GNSSConstants.WGS84_ECCENTRICITY;

		// Radius computation
		double r = Math.sqrt(Math.pow(X, 2) + Math.pow(Y, 2) + Math.pow(Z, 2));

		// Geocentric longitude
		double lamGeoc = Math.atan2(Y, X);

		// Geocentric latitude
		double phiGeoc = Math.atan(Z / Math.sqrt(Math.pow(X, 2) + Math.pow(Y, 2)));

		// Computation of geodetic coordinates
		double psi = Math.atan(Math.tan(phiGeoc) / Math.sqrt(1 - Math.pow(e, 2)));
		double phiGeod = Math.atan((r * Math.sin(phiGeoc) + Math.pow(e, 2) * a
				/ Math.sqrt(1 - Math.pow(e, 2)) * Math.pow(Math.sin(psi), 3))
				/ (r * Math.cos(phiGeoc) - Math.pow(e, 2) * a * Math.pow(Math.cos(psi), 3)));
		double lamGeod = lamGeoc;
		double N = a / Math.sqrt(1 - Math.pow(e, 2) * Math.pow(Math.sin(phiGeod), 2));
		double h = r * Math.cos(phiGeoc) / Math.cos(phiGeod) - N;

		this.geod.set(0, 0, Math.toDegrees(lamGeod));
		this.geod.set(1, 0, Math.toDegrees(phiGeod));
		this.geod.set(2, 0, h);
	}

	/*
	 function [X,Y,Z] = frgeod( a, finv, dphi, dlambda, h )
	     %FRGEOD  Subroutine to calculate Cartesian coordinates X,Y,Z
	     %       given geodetic coordinates latitude, longitude (east),
	     %       and height above reference ellipsoid along with
	     %       reference ellipsoid values semi-major axis (a) and
	     %       the inverse of flattening (finv)

	     % The units of linear parameters h,a must agree (m,km,mi,..etc).
	     % The input units of angular quantities must be in decimal degrees.
	     % The output units of X,Y,Z will be the same as the units of h and a.
	     % Copyright (C) 1987 C. Goad, Columbus, Ohio
	     % Reprinted with permission of author, 1996
	     % Original Fortran code rewritten into MATLAB
	     % Kai Borre 03-03-96
	 */
	public void computeECEF() {
		final long a = 6378137;
		final double finv = 298.257223563d;

		double dphi = this.geod.get(1);
		double dlambda = this.geod.get(0);
		double h = this.geod.get(2);

		// compute degree-to-radian factor
		double dtr = Math.PI/180;

		// compute square of eccentricity
		double esq = (2-1/finv)/finv;
		double sinphi = Math.sin(dphi*dtr);
		// compute radius of curvature in prime vertical
		double N_phi = a/ Math.sqrt(1-esq*sinphi*sinphi);

		// compute P and Z
		// P is distance from Z axis
		double P = (N_phi + h)* Math.cos(dphi*dtr);
		double Z = (N_phi*(1-esq) + h) * sinphi;
		double X = P* Math.cos(dlambda*dtr);
		double Y = P* Math.sin(dlambda*dtr);

		this.ecef.set(0, 0, X );
		this.ecef.set(1, 0, Y );
		this.ecef.set(2, 0, Z );
	}

	/**
	 * @param target
	 * @return Local (ENU) coordinates
	 */
	public void computeLocal(Coordinates target) {
		if(this.geod==null) computeGeodetic();

		SimpleMatrix R = rotationMatrix(this);

		enu = R.mult(target.minusXYZ(this));

	}
	
	public void computeLocalV2(Coordinates target) {
		if(this.geod==null) computeGeodetic();

		SimpleMatrix R = rotationMatrix(this);

		enu = R.mult(target.minusXYZ(this));

	}
	

	public double getGeodeticLongitude(){
		if(this.geod==null) computeGeodetic();
		return this.geod.get(0);
	}
	public double getGeodeticLatitude(){
		if(this.geod==null) computeGeodetic();
		return this.geod.get(1);
	}
	public double getGeodeticHeight(){
		if(this.geod==null) computeGeodetic();
		return this.geod.get(2);
	}
	public double getX(){
		return ecef.get(0);
	}
	public double getY(){
		return ecef.get(1);
	}
	public double getZ(){
		return ecef.get(2);
	}

	public void setENU(double e, double n, double u){
		this.enu.set(0, 0, e);
		this.enu.set(1, 0, n);
		this.enu.set(2, 0, u);
	}
	public double getE(){
		return enu.get(0);
	}
	public double getN(){
		return enu.get(1);
	}
	public double getU(){
		return enu.get(2);
	}


	public void setXYZ(double x, double y, double z){
		//if(this.ecef==null) this.ecef = new SimpleMatrix(3, 1);
		this.ecef.set(0, 0, x);
		this.ecef.set(1, 0, y);
		this.ecef.set(2, 0, z);
	}
	public void setGeod( double lat, double lon, double alt ){
		//if(this.ecef==null) this.ecef = new SimpleMatrix(3, 1);
		this.geod.set(1, 0, lat);
		this.geod.set(0, 0, lon);
		this.geod.set(2, 0, alt);
	}
	public void setPlusXYZ(SimpleMatrix sm){
		this.ecef.set(ecef.plus(sm));
	}
	public void setSMMultXYZ(SimpleMatrix sm){
		this.ecef = sm.mult(this.ecef);
	}

	public boolean isValidXYZ(){
		return (this.ecef != null && this.ecef.elementSum() != 0 
        && !Double.isNaN(this.ecef.get(0)) && !Double.isNaN(this.ecef.get(1)) && !Double.isNaN(this.ecef.get(2))
        && !Double.isInfinite(this.ecef.get(0)) && !Double.isInfinite(this.ecef.get(1)) && !Double.isInfinite(this.ecef.get(2))
        && ( ecef.get(0) != 0 && ecef.get(1)!=0 && ecef.get(2)!= 0 )
		    );
	}

	public Object clone(){
		Coordinates c = new Coordinates();
		cloneInto(c);
		return c;
	}

	public void cloneInto(Coordinates c){
		c.ecef = this.ecef.copy();
		c.enu = this.enu.copy();
		c.geod = this.geod.copy();

		if(refTime!=null) c.refTime = (Time)refTime.clone();
	}
	/**
	 * @param origin
	 * @return Rotation matrix used to switch from global to local reference systems (and vice-versa)
	 */
	public static SimpleMatrix rotationMatrix(Coordinates origin) {

		double lam = Math.toRadians(origin.getGeodeticLongitude());
		double phi = Math.toRadians(origin.getGeodeticLatitude());

		double cosLam = Math.cos(lam);
		double cosPhi = Math.cos(phi);
		double sinLam = Math.sin(lam);
		double sinPhi = Math.sin(phi);

		double[][] data = new double[3][3];
		data[0][0] = -sinLam;
		data[0][1] = cosLam;
		data[0][2] = 0;
		data[1][0] = -sinPhi * cosLam;
		data[1][1] = -sinPhi * sinLam;
		data[1][2] = cosPhi;
		data[2][0] = cosPhi * cosLam;
		data[2][1] = cosPhi * sinLam;
		data[2][2] = sinPhi;

		SimpleMatrix R = new SimpleMatrix(data);

		return R;
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


	public String toString(){
		String lineBreak = System.getProperty("line.separator");

		String out= String.format( "Coord ECEF: X:"+getX()+" Y:"+getY()+" Z:"+getZ()+lineBreak +
		"       ENU: E:"+getE()+" N:"+getN()+" U:"+getU()+lineBreak +
		"      GEOD: Lon:"+getGeodeticLongitude()+" Lat:"+getGeodeticLatitude()+" H:"+getGeodeticHeight()+lineBreak +
		"      http://maps.google.com?q=%3.4f,%3.4f" + lineBreak, getGeodeticLatitude(), getGeodeticLongitude() );
		return out;
	}



	/**
	 * 主要实现在wgs84椭球下  大地坐标系转为WGS84空间直角坐标系
	 * 白腾飞
	 * 202.3.26
	 */
	public static double[] WGS84LLAtoXYZ(double Lat, double Long, double Alt) {
		double PI = Math.PI;
		double API = Math.PI / 180.0;
		final double a = 6378137.0;
		final double f = 1 / 298.257223563;
		final double b = a * (1 - f);
		double e1 = Math.pow(a * a - b * b, 0.5) / a;
		double e2 = Math.pow(a * a - b * b, 0.5) / b;

		double Lat_rad=Lat*API;

		//子午线弧长计算步骤
		double m0=a*(1-Math.pow(e1,2.0));
		double m2 = 1.5 * Math.pow(e1, 2.0) * m0;
		double m4 = 1.25 * Math.pow(e1, 2.0) * m2;
		double m6 = 7.0 / 6.0 * Math.pow(e1, 2.0) * m4;
		double m8 = 9.0 / 8.0 * Math.pow(e1, 2.0) * m6;

		double a0 = m0 + m2 / 2.0 + 3.0 / 8.0 * m4 + 5.0 / 16.0 * m6 + 35.0 / 128.0 * m8;
		double a2 = m2 / 2.0 + m4 / 2.0 + 15.0 / 32.0 * m6 + 7.0 / 16.0 * m8;
		double a4 = m4 / 8.0 + 3.0 / 16.0 * m6 + 7.0 / 32.0 * m8;
		double a6 = m6 / 32.0 + m8 / 16.0;
		double a8 = m8 / 128.0;
		double X = a0 * Lat_rad - a2 / 2.0 * Math.sin(2.0 * Lat_rad) + a4 / 4.0 * Math.sin(4.0 * Lat_rad) - a6 / 6.0 * Math.sin(6.0 * Lat_rad) + a8 / 8.0 * Math.sin(8.0 * Lat_rad);


		//获取三度带的中心子午线
		int n=(int)Math.round(Long/3);
		double Long0=3*n;

		double l=(Long-Long0)*API;


		double t = Math.tan(Lat_rad);
		double η = e2 * Math.cos(Lat_rad);
		double N = a / Math.sqrt(1 - e1 *e1* Math.sin(Lat_rad) * Math.sin(Lat_rad));//卯酉圈半径长度
//        double x=X+N/2*Math.sin(Lat_rad)*Math.cos(Lat_rad)*Math.pow(l,2)+N/24*Math.sin(Lat_rad)*Math.pow(Math.cos(Lat_rad),3)*(5-Math.pow(t,2)+Math.pow(η,2)*9+4*Math.pow(η,4))*Math.pow(l,4)+N/720*Math.sin(Lat_rad)*Math.pow(Math.cos(Lat_rad),5)*(61-58*Math.pow(t,2)+Math.pow(t,4)+270*Math.pow(η,2)-330*Math.pow(η,2)*Math.pow(t,2))*Math.pow(l,6);
//
//        double y = N * l * Math.cos(Lat_rad) + N / 6 * Math.pow(Math.cos(Lat_rad), 3) * (1 - Math.pow(t, 2) + Math.pow(η, 2)) * Math.pow(l, 3) + N / 120 * Math.pow(Math.cos(Lat_rad), 5) * (5 - 18 * Math.pow(t, 2) + Math.pow(t, 4) + 14 * Math.pow(η, 2) - 58 * Math.pow(η, 2) * Math.pow(t, 2)) * Math.pow(l, 5);

		double x=(N+Alt)*Math.cos(Lat_rad)*Math.cos(Long*API);
		double y=(N+Alt)*Math.cos(Lat_rad)*Math.sin(Long*API);
		double z=(N*(1-e1*e1)+Alt)*Math.sin(Lat_rad);
		double[] XYZ=new double[]{x,y,z};
		return XYZ;
	}


}

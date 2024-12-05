package com.example.anew.corrections;

import static com.example.anew.Ntrip.DecodeIonoData.iono;
import com.example.anew.Constellations.Time;
import com.example.anew.coord.Coordinates;
import com.example.anew.coord.SatellitePosition;


public class IonoCorrection_tecgrid {

    private double SumSTEC=0.0;

    public double P(int n,int m,Coordinates approximatedPose, SatellitePosition satelliteCoordinates){
        double p=0.0;
        double latIPP=0.0;
        double central_angle=0.0;
        //直接调用对流层里的方法计算角度
        TopocentricCoordinates topo = new TopocentricCoordinates();
        topo.computeTopocentric(approximatedPose, satelliteCoordinates);

        // Assign the elevation and azimuth information to new variables
        double elevation = topo.getElevation();
        double azimuth = topo.getAzimuth();
        elevation = Math.abs(elevation);
        double lon = approximatedPose.getGeodeticLongitude() / 180; // geod.get(0)
        double lat = approximatedPose.getGeodeticLatitude() / 180; //geod.get(1)
        azimuth = azimuth / 180;
        elevation = elevation / 180;
        //对每层的P进行计算
        for (int lay=0;lay<iono.getLayers();lay++) {
            central_angle = Math.PI / 2 - elevation - Math.asin((6370 + approximatedPose.getGeodeticHeight()) * Math.cos(elevation) / (6370 + iono.getHeight(lay)));
            latIPP = Math.asin(Math.sin(approximatedPose.getGeodeticLatitude()) * Math.cos(central_angle) + Math.cos(approximatedPose.getGeodeticLatitude()) * Math.sin(central_angle) * Math.cos(azimuth));
            //构造P函数的递归算法
            if (n == 0 && m == 0) {
                p = 1;
            } else if (n == 1 && m == 1) {
                p = Math.sqrt(3) * Math.cos(latIPP);
            } else if (n == m && m >= 2) {
                p = Math.sqrt((2.0 * n + 1) / 2.0 * n) * Math.cos(latIPP) * P(n - 1, m - 1, approximatedPose, satelliteCoordinates);
            } else if (n == m + 1 && m >= 0) {
                p = Math.sqrt(2.0 * n + 1) * Math.sin(latIPP) * P(n - 1, m, approximatedPose, satelliteCoordinates);
            } else if (m >= 0 && m <= n - 2 && n >= 2) {
                double a = Math.sqrt((2.0 * n - 1) * (2.0 * n + 1) / ((n - m) * (n + m)));
                double b = Math.sqrt((2.0 * n + 1) * (n + m - 1) * (n - m - 1) / ((n - m) * (n + m) * (2.0 * n - 3)));
                p = a * Math.sin(latIPP) * P(n - 1, m, approximatedPose, satelliteCoordinates) + b * P(n - 2, m, approximatedPose, satelliteCoordinates);
            }
        }
        return p;
    }

    public void calculateCorrection(Time currentTime, Coordinates approximatedPose, SatellitePosition satelliteCoordinates) {
        double STEC=0.0;
        double VTEC=0.0;
        double lonIPP=0.0;
        double latIPP=0.0;
        double central_angle=0.0;
        //直接调用对流层里的方法计算角度
        TopocentricCoordinates topo = new TopocentricCoordinates();
        topo.computeTopocentric(approximatedPose, satelliteCoordinates);
        double elevation = topo.getElevation();
        double azimuth = topo.getAzimuth();
        elevation = Math.abs(elevation);
        double lon = approximatedPose.getGeodeticLongitude() / 180; // geod.get(0)
        double lat = approximatedPose.getGeodeticLatitude() / 180; //geod.get(1)
        azimuth = azimuth / 180;
        elevation = elevation / 180;
        //对每层的STEC进行计算
        for (int lay=0;lay<iono.getLayers();lay++) {
            central_angle=Math.PI/2-elevation-Math.asin((6370+approximatedPose.getGeodeticHeight())*Math.cos(elevation)/(6370+iono.getHeight(lay)));
            latIPP=Math.asin(Math.sin(approximatedPose.getGeodeticLatitude())*Math.cos(central_angle)+Math.cos(approximatedPose.getGeodeticLatitude())*Math.sin(central_angle)*Math.cos(azimuth));

            if(approximatedPose.getGeodeticLatitude()>0&&(Math.tan(central_angle)*Math.cos(azimuth)>Math.tan(0.5*Math.PI-approximatedPose.getGeodeticLatitude()))){
                lonIPP=approximatedPose.getGeodeticLongitude()+Math.PI-Math.asin(Math.sin(central_angle)*Math.sin(azimuth)/Math.cos(latIPP));
            }
            else if(approximatedPose.getGeodeticLatitude()<0&&(-Math.tan(central_angle)*Math.cos(azimuth)>Math.tan(0.5*Math.PI+approximatedPose.getGeodeticLatitude()))) {
                lonIPP=approximatedPose.getGeodeticLongitude()+Math.PI-Math.asin(Math.sin(central_angle)*Math.sin(azimuth)/Math.cos(latIPP));
            }
            else{
                lonIPP=approximatedPose.getGeodeticLongitude()-Math.asin(Math.sin(central_angle)*Math.sin(azimuth)/Math.cos(latIPP));
            }
            double lonS= lonIPP+(currentTime.getGpsTime()-50400)*Math.PI/43200;
            for (int n = 0; n < iono.getDegree(lay) + 1; n++) {
                for (int m = 0; m < Math.min(n, iono.getOrder(lay)) + 1; m++) {
                    if (m != 0) {
                        VTEC = VTEC + (iono.getCosineC(n, m) * Math.cos(m * lonS) + iono.getSineS(n, m) * Math.sin(m * lonS)) * P(n, m, approximatedPose, satelliteCoordinates);
                    } else if (m == 0) {
                        VTEC = VTEC + iono.getCosineC(n, m) * Math.cos(m * lonS) * P(n, m, approximatedPose, satelliteCoordinates);
                    }
                }
            }
            STEC=STEC+VTEC/Math.sin(elevation+central_angle);
            VTEC=0.0;
        }
        SumSTEC=STEC;
    }


    public double getCorrection() {
        return SumSTEC;
    }


    public String getName() {
        return null;
    }
}

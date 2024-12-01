package com.example.anew.Satellites;

/**
 * bds卫星数据
 * 2020/3/15
 * butterflying10
 */

import com.example.anew.GNSSConstants;

/**
 * rinex2.11 不存在   rinex3.03  C2I  L2I   S2I   D2I
 */
public class BdsSatellite{


    private String prn;

    public String getPrn() {
        return prn;
    }

    public void setPrn(String prn) {
        this.prn = prn;
    }


    private double C2I;

    public double getC2I() {
        return C2I;
    }

    public void setC2I(double c2I) {
        C2I = c2I;
    }

    private double L2I;

    public double getL2I() {
        return L2I;
    }

    public void setL2I(double l2I) {
        L2I = l2I;
    }

    private double S2I;

    public void setS2I(double s2I) {
        S2I = s2I;
    }

    public double getS2I() {
        return S2I;
    }

    private double D2I;

    public void setD2I(double d2I) {
        D2I = d2I;
    }

    public double getD2I() {
        return D2I;
    }

    private double C1;
    private double C5;

    public double getC1() {
        return C1;
    }
    public void setC1(double c1) {
        C1 = c1;
    }
    public double getC5() {
        return C5;
    }
    public void setC5(double c5) {
        C5 = c5;
    }

    private double L1;
    private double L5;

    public double getL1() {
        return L1;
    }
    public void setL1(double l1) {
        L1 = l1;
    }
    public double getL5() {
        return L5;
    }
    public void setL5(double l5) {
        L5 = l5;
    }

    private double S1;
    private double S5;

    public double getS1() {
        return S1;
    }
    public void setS1(double s1) {
        S1 = s1;
    }
    public double getS5() {
        return S5;
    }
    public void setS5(double s5) {
        S5 = s5;
    }

    private double D1;
    private double D5;

    public double getD1() {
        return D1;
    }
    public void setD1(double d1) {
        D1 = d1;
    }
    public double getD5() {
        return D5;
    }
    public void setD5(double d5) {
        D5 = d5;
    }

    public boolean isHasC1() {
        if (this.getC1() != 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isHasC5() {
        if (this.getC5() != 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isHasC2I() {
        if (this.getC2I() != 0) {
            return true;
        } else {
            return false;
        }
    }

    private double P_IF;

    public double getP_IF() {
        return P_IF;
    }

    public void setP_IF() {
        double f1_squared = GNSSConstants.FL1 * GNSSConstants.FL1;
        double f5_squared = GNSSConstants.FL5 * GNSSConstants.FL5;
        double denominator = f1_squared - f5_squared;
        P_IF = (f1_squared / denominator) * C1 - (f5_squared / denominator) * C5;
    }
}

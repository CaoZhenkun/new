package com.example.anew.Ntrip;

import com.example.anew.Constellations.Time;

public class Iono {

    private Time ephTime;

    private double iod;//数据期龄：数据可用的起始时间与终止时间之差值。

    private double qual;//电离层质量指示

    private int layers;//电离层层数

    private double[] height=new double[layers];//电离层高度

    private int []degree=new int[layers];

    private int []order=new int[layers];

    private double[][] cosineC=new double[degree[layers]][order[layers]];//余弦系数C

    private double[][] sineS=new double[degree[layers]][order[layers]];//正弦系数S

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

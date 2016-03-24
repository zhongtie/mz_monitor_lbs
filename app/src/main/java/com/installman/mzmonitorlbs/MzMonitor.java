package com.installman.mzmonitorlbs;

import java.io.Serializable;

/**
 * Created by zhong on 16-3-24.
 */
public class MzMonitor implements Serializable{
    public enum eMonitorType{BALL, GUN, SMART}
    protected eMonitorType mMonitorType;//监控头类型
    protected int mrkAngle;//监控头方向
    protected String mrkTitle;//监控头名称
    protected double dLocLat, dLocLng;//监控头所在位置

    public void MzMonitor(){
        mMonitorType = eMonitorType.BALL;
        mrkAngle = 0;
        mrkTitle = "广东省梅州市梅江区";
        dLocLat = 116.129435;
        dLocLng = 24.2941424;
    }

    public void MzMonitor(eMonitorType monitorType, int angle, String title, double latitude, double longitude){
        mMonitorType = monitorType;
        mrkAngle = angle;
        mrkTitle = title;
        dLocLat = latitude;
        dLocLng = longitude;
    }

    public eMonitorType getmMonitorType() {
        return mMonitorType;
    }

    public void setmMonitorType(eMonitorType mMonitorType) {
        this.mMonitorType = mMonitorType;
    }

    public int getMrkAngle() {
        return mrkAngle;
    }

    public void setMrkAngle(int mrkAngle) {
        this.mrkAngle = mrkAngle;
    }

    public String getMrkTitle() {
        return mrkTitle;
    }

    public void setMrkTitle(String mrkTitle) {
        this.mrkTitle = mrkTitle;
    }

    public double getdLocLat() {
        return dLocLat;
    }

    public void setdLocLat(double dLocLat) {
        this.dLocLat = dLocLat;
    }

    public double getdLocLng() {
        return dLocLng;
    }

    public void setdLocLng(double dLocLng) {
        this.dLocLng = dLocLng;
    }
}

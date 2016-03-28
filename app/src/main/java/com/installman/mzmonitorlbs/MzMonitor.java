package com.installman.mzmonitorlbs;

import java.io.Serializable;

/**
 * Created by zhong on 16-3-24.
 */
public class MzMonitor implements Serializable{
    public enum eMonitorType{BALL, GUN, SMART}
    protected eMonitorType gMonitorType;//监控头类型
    protected int gMrkAngle;//监控头方向
    protected String gMrkTitle;//监控头名称
    protected double gLocLatitude, gLocLngitude;//监控头所在位置

    public void MzMonitor(){
        gMonitorType = eMonitorType.BALL;
        gMrkAngle = 0;
        gMrkTitle = "广东省梅州市梅江区";
        gLocLatitude = 116.129435;
        gLocLngitude = 24.2941424;
    }

    public void MzMonitor(eMonitorType monitorType, int angle, String title, double latitude, double longitude){
        gMonitorType = monitorType;
        gMrkAngle = angle;
        gMrkTitle = title;
        gLocLatitude = latitude;
        gLocLngitude = longitude;
    }

    public eMonitorType getMonitorType() {
        return gMonitorType;
    }

    public void setMonitorType(eMonitorType gMonitorType) {
        this.gMonitorType = gMonitorType;
    }

    public int getMrkAngle() {
        return gMrkAngle;
    }

    public void setMrkAngle(int gMrkAngle) {
        this.gMrkAngle = gMrkAngle;
    }

    public String getMrkTitle() {
        return gMrkTitle;
    }

    public void setMrkTitle(String gMrkTitle) {
        this.gMrkTitle = gMrkTitle;
    }

    public double getLocLatitude() {
        return gLocLatitude;
    }

    public void setLocLatitude(double gLocLatitude) {
        this.gLocLatitude = gLocLatitude;
    }

    public double getLocLngitude() {
        return gLocLngitude;
    }

    public void setLocLngitude(double gLocLngitude) {
        this.gLocLngitude = gLocLngitude;
    }
}

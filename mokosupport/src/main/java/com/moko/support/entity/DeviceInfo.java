package com.moko.support.entity;

import java.io.Serializable;

/**
 * @Date 2017/12/28
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.entity.DeviceInfo
 */
public class DeviceInfo implements Serializable {
    public String name;
    public int rssi;
    public String mac;
    public String scanRecord;
    public String saveDate;
    public String productDate;
    public String version;


    @Override
    public String toString() {
        return "DeviceInfo{" +
                "name='" + name + '\'' +
                ", rssi=" + rssi +
                ", mac='" + mac + '\'' +
                ", scanRecord='" + scanRecord + '\'' +
                ", saveDate='" + saveDate + '\'' +
                ", productDate='" + productDate + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}

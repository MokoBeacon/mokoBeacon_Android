package com.moko.beacon.entity;

import java.io.Serializable;

/**
 * @Date 2017/12/8 0008
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beacon.entity.BeaconInfo
 */
public class BeaconInfo implements Serializable{
    public String name;
    public int rssi;
    public String distance;
    public String distanceDesc;
    public int major;
    public int minor;
    public boolean isConnected;
    public int txPower;
    public String mac;
    public String uuid;
    public int batteryPower;
    public int version;
    public String scanRecord;

    @Override
    public String toString() {
        return "BeaconInfo{" +
                "name='" + name + '\'' +
                ", rssi=" + rssi +
                ", distance='" + distance + '\'' +
                ", distanceDesc='" + distanceDesc + '\'' +
                ", major=" + major +
                ", minor=" + minor +
                ", isConnected='" + isConnected + '\'' +
                ", txPower=" + txPower +
                ", mac='" + mac + '\'' +
                ", uuid='" + uuid + '\'' +
                ", batteryPower=" + batteryPower +
                ", version=" + version +
                ", scanRecord='" + scanRecord + '\'' +
                '}';
    }
}

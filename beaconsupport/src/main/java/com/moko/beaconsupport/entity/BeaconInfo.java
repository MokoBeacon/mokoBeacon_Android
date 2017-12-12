package com.moko.beaconsupport.entity;

/**
 * @Date 2017/12/8 0008
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beacon.entity.BeaconInfo
 */
public class BeaconInfo {
    public String name;
    public int rssi;
    public String distance;
    public String distanceDesc;
    public int major;
    public int minor;
    public String connState;
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
                ", connState='" + connState + '\'' +
                ", txPower=" + txPower +
                ", mac='" + mac + '\'' +
                ", uuid='" + uuid + '\'' +
                ", batteryPower=" + batteryPower +
                ", version=" + version +
                ", scanRecord='" + scanRecord + '\'' +
                '}';
    }
}

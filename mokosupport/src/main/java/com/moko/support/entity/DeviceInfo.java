package com.moko.support.entity;

import java.io.Serializable;

import no.nordicsemi.android.support.v18.scanner.ScanResult;

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
    public ScanResult scanResult;

    @Override
    public String toString() {
        return "BeaconInfo{" +
                "name='" + name + '\'' +
                ", rssi=" + rssi +
                ", mac='" + mac + '\'' +
                ", scanRecord='" + scanRecord + '\'' +
                '}';
    }
}

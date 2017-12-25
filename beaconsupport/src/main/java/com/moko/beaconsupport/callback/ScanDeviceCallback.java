package com.moko.beaconsupport.callback;

import com.moko.beaconsupport.entity.BeaconInfo;

/**
 * @Date 2017/12/8 0008
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beaconsupport.callback.ScanDeviceCallback
 */
public interface ScanDeviceCallback {
    void onStartScan();

    void onScanDevice(BeaconInfo beaconInfos);

    void onStopScan();
}

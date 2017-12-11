package com.moko.beaconsupport.callback;

import android.bluetooth.BluetoothDevice;

/**
 * @Date 2017/12/8 0008
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beaconsupport.callback.ScanDeviceCallback
 */
public interface ScanDeviceCallback {
    void onStartScan();

    void onScanDevice(BluetoothDevice device, int rssi, byte[] scanRecord);

    void onStopScan();
}

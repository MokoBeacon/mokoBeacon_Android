package com.moko.beaconsupport.beacon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.text.TextUtils;

import com.moko.beaconsupport.callback.ScanDeviceCallback;
import com.moko.beaconsupport.log.LogModule;

/**
 * @Date 2017/12/7 0007
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beaconsupport.beacon.BeaconModule
 */
public class BeaconModule {
    private BluetoothAdapter mBluetoothAdapter;
    private FitLeScanCallback mFitLeScanCallback;

    private static volatile BeaconModule INSTANCE;

    public static BeaconModule getInstance() {
        if (INSTANCE == null) {
            synchronized (BeaconModule.class) {
                if (INSTANCE == null) {
                    INSTANCE = new BeaconModule();
                }
            }
        }
        return INSTANCE;
    }

    public void init(Context context) {
        LogModule.init(context);
        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    public void startScanDevice(final ScanDeviceCallback scanDeviceCallback) {
        mFitLeScanCallback = new FitLeScanCallback(scanDeviceCallback);
        mBluetoothAdapter.startLeScan(mFitLeScanCallback);
        scanDeviceCallback.onStartScan();
    }

    public void stopScanDevice() {
        if (mFitLeScanCallback != null) {
            mBluetoothAdapter.stopLeScan(mFitLeScanCallback);
        }
    }

    class FitLeScanCallback implements BluetoothAdapter.LeScanCallback {
        private ScanDeviceCallback callback;

        public FitLeScanCallback(ScanDeviceCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (device != null) {
                if (TextUtils.isEmpty(device.getName()) || scanRecord.length == 0 || rssi == 127) {
                    return;
                }
                callback.onScanDevice(device, rssi, scanRecord);
            }
        }
    }
}

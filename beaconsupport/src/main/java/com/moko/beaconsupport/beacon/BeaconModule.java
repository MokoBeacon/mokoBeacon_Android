package com.moko.beaconsupport.beacon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import com.moko.beaconsupport.callback.FitLeScanCallback;
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

    private BeaconModule() {
    }

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

    public boolean isBluetoothOpen() {
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }

    public void startScanDevice(ScanDeviceCallback scanDeviceCallback) {
        mFitLeScanCallback = new FitLeScanCallback(scanDeviceCallback);
        mBluetoothAdapter.startLeScan(mFitLeScanCallback);
        scanDeviceCallback.onStartScan();
    }

    public void stopScanDevice() {
        if (mFitLeScanCallback != null) {
            mBluetoothAdapter.stopLeScan(mFitLeScanCallback);
        }
    }
}

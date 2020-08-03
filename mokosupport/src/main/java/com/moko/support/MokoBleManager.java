package com.moko.support;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.support.annotation.NonNull;

import com.moko.support.callback.MokoResponseCallback;
import com.moko.support.log.LogModule;
import com.moko.support.utils.MokoUtils;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.BleManagerCallbacks;

public class MokoBleManager extends BleManager<BleManagerCallbacks> {

    private MokoResponseCallback mMokoResponseCallback;
    private static MokoBleManager managerInstance = null;

    public static synchronized MokoBleManager getMokoBleManager(final Context context) {
        if (managerInstance == null) {
            managerInstance = new MokoBleManager(context);
        }
        return managerInstance;
    }

    @Override
    public void log(int priority, @NonNull String message) {
        LogModule.v(message);
    }

    public MokoBleManager(@NonNull Context context) {
        super(context);
    }

    public void setBeaconResponseCallback(MokoResponseCallback mMokoResponseCallback) {
        this.mMokoResponseCallback = mMokoResponseCallback;
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return new MokoBleManagerGattCallback();
    }

    public class MokoBleManagerGattCallback extends BleManagerGattCallback {
        @Override
        protected void initialize() {
            enableBatteryLevelNotifications();
            readBatteryLevel();
        }

        @Override
        protected boolean isRequiredServiceSupported(@NonNull BluetoothGatt gatt) {
            return true;
        }

        @Override
        protected void onDeviceDisconnected() {

        }

        @Override
        protected void onCharacteristicNotified(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic) {
            LogModule.e("onCharacteristicChanged");
            LogModule.e("device to app : " + MokoUtils.bytesToHexString(characteristic.getValue()));
            mMokoResponseCallback.onCharacteristicChanged(characteristic, characteristic.getValue());
        }

        @Override
        protected void onCharacteristicWrite(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic) {
            LogModule.e("device to app : " + MokoUtils.bytesToHexString(characteristic.getValue()));
            mMokoResponseCallback.onCharacteristicWrite(characteristic.getValue());
        }

        @Override
        protected void onCharacteristicRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic) {
            LogModule.e("device to app : " + MokoUtils.bytesToHexString(characteristic.getValue()));
            mMokoResponseCallback.onCharacteristicRead(characteristic.getValue());
        }

        @Override
        protected void onDescriptorWrite(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattDescriptor descriptor) {
            mMokoResponseCallback.onDescriptorWrite();
        }

        @Override
        protected void onBatteryValueReceived(@NonNull BluetoothGatt gatt, int value) {
            LogModule.e(String.format("Battery:%d", value));
            mMokoResponseCallback.onBatteryValueReceived(gatt);
        }
    }
}

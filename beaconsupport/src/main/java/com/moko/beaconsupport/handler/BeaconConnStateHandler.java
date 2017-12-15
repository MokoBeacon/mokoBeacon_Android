package com.moko.beaconsupport.handler;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;

import com.moko.beaconsupport.beacon.BeaconModule;
import com.moko.beaconsupport.callback.BeaconResponseCallback;
import com.moko.beaconsupport.log.LogModule;
import com.moko.beaconsupport.utils.Utils;

/**
 * @Date 2017/5/10
 * @Author wenzheng.liu
 * @Description 自定义蓝牙连接回调
 * @ClassPath com.fitpolo.support.bluetooth.CustomGattCallback
 */
public class BeaconConnStateHandler extends BluetoothGattCallback {

    private static volatile BeaconConnStateHandler INSTANCE;

    private BeaconResponseCallback mBeaconResponseCallback;
    private BeaconModule.ServiceHandler mHandler;

    public BeaconConnStateHandler() {
    }

    public static BeaconConnStateHandler getInstance() {
        if (INSTANCE == null) {
            synchronized (BeaconConnStateHandler.class) {
                if (INSTANCE == null) {
                    INSTANCE = new BeaconConnStateHandler();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        LogModule.e("onConnectionStateChange");
        LogModule.e("status : " + status);
        LogModule.e("newState : " + newState);
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mHandler.sendEmptyMessage(BeaconModule.HANDLER_MESSAGE_WHAT_CONNECTED);
                return;
            }
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            mHandler.sendEmptyMessage(BeaconModule.HANDLER_MESSAGE_WHAT_DISCONNECTED);
            return;
        }
        mHandler.sendEmptyMessage(BeaconModule.HANDLER_MESSAGE_WHAT_DISCONNECTED);
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        LogModule.e("onServicesDiscovered");
        LogModule.e("status : " + status);
        if (status == BluetoothGatt.GATT_SUCCESS) {
            mHandler.sendEmptyMessage(BeaconModule.HANDLER_MESSAGE_WHAT_SERVICES_DISCOVERED);
        } else {
            mHandler.sendEmptyMessage(BeaconModule.HANDLER_MESSAGE_WHAT_DISCONNECTED);
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        LogModule.e("onCharacteristicChanged");
        LogModule.e("ibeacon to app : " + Utils.bytesToHexString(characteristic.getValue()));
        mBeaconResponseCallback.onCharacteristicChanged(characteristic.getValue());
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        LogModule.e("ibeacon to app : " + Utils.bytesToHexString(characteristic.getValue()));
        mBeaconResponseCallback.onCharacteristicWrite(characteristic.getValue());
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        LogModule.e("ibeacon to app : " + Utils.bytesToHexString(characteristic.getValue()));
        mBeaconResponseCallback.onCharacteristicRead(characteristic.getValue());
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
        mBeaconResponseCallback.onDescriptorWrite();
    }

    public void setBeaconResponseCallback(BeaconResponseCallback mBeaconResponseCallback) {
        this.mBeaconResponseCallback = mBeaconResponseCallback;
    }

    public void setMessageHandler(BeaconModule.ServiceHandler messageHandler) {
        this.mHandler = messageHandler;
    }
}

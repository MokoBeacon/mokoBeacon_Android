package com.moko.support.handler;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import com.moko.support.entity.OrderCHAR;
import com.moko.support.entity.OrderServices;

import java.util.HashMap;

public class MokoCharacteristicHandler {
    private HashMap<OrderCHAR, BluetoothGattCharacteristic> mCharacteristicMap;

    public MokoCharacteristicHandler() {
        //no instance
        mCharacteristicMap = new HashMap<>();
    }

    public HashMap<OrderCHAR, BluetoothGattCharacteristic> getCharacteristics(final BluetoothGatt gatt) {
        if (mCharacteristicMap != null && !mCharacteristicMap.isEmpty()) {
            mCharacteristicMap.clear();
        }
        if (gatt.getService(OrderServices.SERVICE_BATTERY.getUuid()) != null) {
            final BluetoothGattService service = gatt.getService(OrderServices.SERVICE_BATTERY.getUuid());
            if (service.getCharacteristic(OrderCHAR.CHAR_BATTERY.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_BATTERY.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_BATTERY, characteristic);
            }
        }
        if (gatt.getService(OrderServices.SERVICE_DEVICE_INFO.getUuid()) != null) {
            final BluetoothGattService service = gatt.getService(OrderServices.SERVICE_DEVICE_INFO.getUuid());
            if (service.getCharacteristic(OrderCHAR.CHAR_MODEL_NUMBER.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_MODEL_NUMBER.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_MODEL_NUMBER, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_SERIAL_NUMBER.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_SERIAL_NUMBER.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_SERIAL_NUMBER, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_FIRMWARE_REVISION.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_FIRMWARE_REVISION.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_FIRMWARE_REVISION, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_HARDWARE_REVISION.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_HARDWARE_REVISION.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_HARDWARE_REVISION, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_SOFTWARE_REVISION.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_SOFTWARE_REVISION.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_SOFTWARE_REVISION, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_MANUFACTURER_NAME.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_MANUFACTURER_NAME.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_MANUFACTURER_NAME, characteristic);
            }
        }
        if (gatt.getService(OrderServices.SERVICE_CUSTOM.getUuid()) != null) {
            final BluetoothGattService service = gatt.getService(OrderServices.SERVICE_CUSTOM.getUuid());
            if (service.getCharacteristic(OrderCHAR.CHAR_PASSWORD.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_PASSWORD.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_PASSWORD, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_PARAMS.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_PARAMS.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_PARAMS, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_DEVICE_UUID.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_DEVICE_UUID.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_DEVICE_UUID, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_MAJOR.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_MAJOR.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_MAJOR, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_MINOR.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_MINOR.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_MINOR, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_MINOR.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_MINOR.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_MINOR, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_MEASURE_POWER.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_MEASURE_POWER.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_MEASURE_POWER, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_TRANSMISSION.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_TRANSMISSION.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_TRANSMISSION, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_ADV_INTERVAL.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_ADV_INTERVAL.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_ADV_INTERVAL, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_SERIAL_ID.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_SERIAL_ID.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_SERIAL_ID, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_ADV_NAME.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_ADV_NAME.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_ADV_NAME, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_CONNECTION.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_CONNECTION.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_CONNECTION, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_SOFT_REBOOT.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_SOFT_REBOOT.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_SOFT_REBOOT, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_DEVICE_MAC.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_DEVICE_MAC.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_DEVICE_MAC, characteristic);
            }
            if (service.getCharacteristic(OrderCHAR.CHAR_OVER_TIME.getUuid()) != null) {
                final BluetoothGattCharacteristic characteristic = service.getCharacteristic(OrderCHAR.CHAR_OVER_TIME.getUuid());
                mCharacteristicMap.put(OrderCHAR.CHAR_OVER_TIME, characteristic);
            }
        }
        return mCharacteristicMap;
    }
}

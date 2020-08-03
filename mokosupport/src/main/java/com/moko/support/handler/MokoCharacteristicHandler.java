package com.moko.support.handler;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.text.TextUtils;

import com.moko.support.entity.MokoCharacteristic;
import com.moko.support.entity.OrderType;

import java.util.HashMap;
import java.util.List;

/**
 * @Date 2017/12/13 0013
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.handler.MokoCharacteristicHandler
 */
public class MokoCharacteristicHandler {
    private static MokoCharacteristicHandler INSTANCE;

    public static final String SERVICE_UUID_HEADER_BATTERY = "0000180f";
    public static final String SERVICE_UUID_HEADER_SYSTEM = "0000180a";
    public static final String SERVICE_UUID_HEADER_PARAMS = "0000ff00";
    public HashMap<OrderType, MokoCharacteristic> mokoCharacteristicMap;

    private MokoCharacteristicHandler() {
        //no instance
        mokoCharacteristicMap = new HashMap<>();
    }

    public static MokoCharacteristicHandler getInstance() {
        if (INSTANCE == null) {
            synchronized (MokoCharacteristicHandler.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MokoCharacteristicHandler();
                }
            }
        }
        return INSTANCE;
    }

    public HashMap<OrderType, MokoCharacteristic> getCharacteristics(BluetoothGatt gatt) {
        if (mokoCharacteristicMap != null && !mokoCharacteristicMap.isEmpty()) {
            mokoCharacteristicMap.clear();
        }
        List<BluetoothGattService> services = gatt.getServices();
        for (BluetoothGattService service : services) {
            String serviceUuid = service.getUuid().toString();
            if (TextUtils.isEmpty(serviceUuid)) {
                continue;
            }
            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            if (serviceUuid.startsWith(SERVICE_UUID_HEADER_BATTERY)) {
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    String characteristicUuid = characteristic.getUuid().toString();
                    if (TextUtils.isEmpty(characteristicUuid)) {
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.BATTERY.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.BATTERY, new MokoCharacteristic(characteristic, OrderType.BATTERY));
                        continue;
                    }
                }
            }
            if (service.getUuid().toString().startsWith(SERVICE_UUID_HEADER_SYSTEM)) {
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    String characteristicUuid = characteristic.getUuid().toString();
                    if (TextUtils.isEmpty(characteristicUuid)) {
                        continue;
                    }
                    // 软件版本
                    if (characteristicUuid.equals(OrderType.SOFT_VERSION.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.SOFT_VERSION, new MokoCharacteristic(characteristic, OrderType.SOFT_VERSION));
                        continue;
                    }
                    // 厂商名称
                    if (characteristicUuid.equals(OrderType.MANUFACTURER.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.MANUFACTURER, new MokoCharacteristic(characteristic, OrderType.MANUFACTURER));
                        continue;
                    }
                    // 设备名称
                    if (characteristicUuid.equals(OrderType.DEVICE_MODEL.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.DEVICE_MODEL, new MokoCharacteristic(characteristic, OrderType.DEVICE_MODEL));
                        continue;
                    }
                    // 出厂日期
                    if (characteristicUuid.equals(OrderType.PRODUCT_DATE.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.PRODUCT_DATE, new MokoCharacteristic(characteristic, OrderType.PRODUCT_DATE));
                        continue;
                    }
                    // 硬件版本号
                    if (characteristicUuid.equals(OrderType.HARDWARE_VERSION.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.HARDWARE_VERSION, new MokoCharacteristic(characteristic, OrderType.HARDWARE_VERSION));
                        continue;
                    }
                    // 固件版本号
                    if (characteristicUuid.equals(OrderType.FIRMWARE_VERSION.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.FIRMWARE_VERSION, new MokoCharacteristic(characteristic, OrderType.FIRMWARE_VERSION));
                        continue;
                    }
                }
            }
            if (service.getUuid().toString().startsWith(SERVICE_UUID_HEADER_PARAMS)) {
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    String characteristicUuid = characteristic.getUuid().toString();
                    if (TextUtils.isEmpty(characteristicUuid)) {
                        continue;
                    }
                    // 写和通知
                    if (characteristicUuid.equals(OrderType.PARAMS_CONFIG.getUuid())) {
                        gatt.setCharacteristicNotification(characteristic, true);
                        mokoCharacteristicMap.put(OrderType.PARAMS_CONFIG, new MokoCharacteristic(characteristic, OrderType.PARAMS_CONFIG));
                        continue;
                    }
                    // uuid
                    if (characteristicUuid.equals(OrderType.DEVICE_UUID.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.DEVICE_UUID, new MokoCharacteristic(characteristic, OrderType.DEVICE_UUID));
                        continue;
                    }
                    // major
                    if (characteristicUuid.equals(OrderType.MAJOR.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.MAJOR, new MokoCharacteristic(characteristic, OrderType.MAJOR));
                        continue;
                    }
                    // minor
                    if (characteristicUuid.equals(OrderType.MINOR.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.MINOR, new MokoCharacteristic(characteristic, OrderType.MINOR));
                        continue;
                    }
                    // measure_power
                    if (characteristicUuid.equals(OrderType.MEASURE_POWER.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.MEASURE_POWER, new MokoCharacteristic(characteristic, OrderType.MEASURE_POWER));
                        continue;
                    }
                    // transmission
                    if (characteristicUuid.equals(OrderType.TRANSMISSION.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.TRANSMISSION, new MokoCharacteristic(characteristic, OrderType.TRANSMISSION));
                        continue;
                    }
                    // change_password
                    if (characteristicUuid.equals(OrderType.PASSWORD.getUuid())) {
                        gatt.setCharacteristicNotification(characteristic, true);
                        mokoCharacteristicMap.put(OrderType.PASSWORD, new MokoCharacteristic(characteristic, OrderType.PASSWORD));
                        continue;
                    }
                    // broadcasting_interval
                    if (characteristicUuid.equals(OrderType.ADV_INTERVAL.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.ADV_INTERVAL, new MokoCharacteristic(characteristic, OrderType.ADV_INTERVAL));
                        continue;
                    }
                    // serial_id
                    if (characteristicUuid.equals(OrderType.SERIAL_ID.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.SERIAL_ID, new MokoCharacteristic(characteristic, OrderType.SERIAL_ID));
                        continue;
                    }
                    // iBeacon_name
                    if (characteristicUuid.equals(OrderType.ADV_NAME.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.ADV_NAME, new MokoCharacteristic(characteristic, OrderType.ADV_NAME));
                        continue;
                    }
                    // connection_mode
                    if (characteristicUuid.equals(OrderType.CONNECTION.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.CONNECTION, new MokoCharacteristic(characteristic, OrderType.CONNECTION));
                        continue;
                    }
                    // soft_reboot
                    if (characteristicUuid.equals(OrderType.SOFT_REBOOT.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.SOFT_REBOOT, new MokoCharacteristic(characteristic, OrderType.SOFT_REBOOT));
                        continue;
                    }
                    // iBeacon_mac
                    if (characteristicUuid.equals(OrderType.DEVICE_MAC.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.DEVICE_MAC, new MokoCharacteristic(characteristic, OrderType.DEVICE_MAC));
                        continue;
                    }
                    // overtime
                    if (characteristicUuid.equals(OrderType.OVER_TIME.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.OVER_TIME, new MokoCharacteristic(characteristic, OrderType.OVER_TIME));
                        continue;
                    }
                }
            }
//            LogModule.i("service uuid:" + service.getUuid().toString());
//            List<BluetoothGattCharacteristic> characteristicList = service.getCharacteristics();
//            for (BluetoothGattCharacteristic characteristic : characteristicList) {
//                LogModule.i("characteristic uuid:" + characteristic.getUuid().toString());
//                LogModule.i("characteristic properties:" + MokoUtils.getCharPropertie(characteristic.getProperties()));
//                List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
//                for (BluetoothGattDescriptor descriptor : descriptors) {
//                    LogModule.i("descriptor uuid:" + descriptor.getUuid().toString());
//                    LogModule.i("descriptor value:" + MokoUtils.bytesToHexString(descriptor.getValue()));
//                }
//            }
        }
        return mokoCharacteristicMap;
    }
}

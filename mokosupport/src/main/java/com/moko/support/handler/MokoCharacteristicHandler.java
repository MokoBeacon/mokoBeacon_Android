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
                    if (characteristicUuid.equals(OrderType.battery.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.battery, new MokoCharacteristic(characteristic, OrderType.battery));
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
                    if (characteristicUuid.equals(OrderType.softVersion.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.softVersion, new MokoCharacteristic(characteristic, OrderType.softVersion));
                        continue;
                    }
                    // 厂商名称
                    if (characteristicUuid.equals(OrderType.firmname.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.firmname, new MokoCharacteristic(characteristic, OrderType.firmname));
                        continue;
                    }
                    // 设备名称
                    if (characteristicUuid.equals(OrderType.devicename.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.devicename, new MokoCharacteristic(characteristic, OrderType.devicename));
                        continue;
                    }
                    // 出厂日期
                    if (characteristicUuid.equals(OrderType.iBeaconDate.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.iBeaconDate, new MokoCharacteristic(characteristic, OrderType.iBeaconDate));
                        continue;
                    }
                    // 硬件版本号
                    if (characteristicUuid.equals(OrderType.hardwareVersion.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.hardwareVersion, new MokoCharacteristic(characteristic, OrderType.hardwareVersion));
                        continue;
                    }
                    // 固件版本号
                    if (characteristicUuid.equals(OrderType.firmwareVersion.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.firmwareVersion, new MokoCharacteristic(characteristic, OrderType.firmwareVersion));
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
                    if (characteristicUuid.equals(OrderType.writeAndNotify.getUuid())) {
                        gatt.setCharacteristicNotification(characteristic, true);
                        mokoCharacteristicMap.put(OrderType.writeAndNotify, new MokoCharacteristic(characteristic, OrderType.writeAndNotify));
                        continue;
                    }
                    // uuid
                    if (characteristicUuid.equals(OrderType.iBeaconUuid.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.iBeaconUuid, new MokoCharacteristic(characteristic, OrderType.iBeaconUuid));
                        continue;
                    }
                    // major
                    if (characteristicUuid.equals(OrderType.major.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.major, new MokoCharacteristic(characteristic, OrderType.major));
                        continue;
                    }
                    // minor
                    if (characteristicUuid.equals(OrderType.minor.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.minor, new MokoCharacteristic(characteristic, OrderType.minor));
                        continue;
                    }
                    // measure_power
                    if (characteristicUuid.equals(OrderType.measurePower.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.measurePower, new MokoCharacteristic(characteristic, OrderType.measurePower));
                        continue;
                    }
                    // transmission
                    if (characteristicUuid.equals(OrderType.transmission.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.transmission, new MokoCharacteristic(characteristic, OrderType.transmission));
                        continue;
                    }
                    // change_password
                    if (characteristicUuid.equals(OrderType.changePassword.getUuid())) {
                        gatt.setCharacteristicNotification(characteristic, true);
                        mokoCharacteristicMap.put(OrderType.changePassword, new MokoCharacteristic(characteristic, OrderType.changePassword));
                        continue;
                    }
                    // broadcasting_interval
                    if (characteristicUuid.equals(OrderType.broadcastingInterval.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.broadcastingInterval, new MokoCharacteristic(characteristic, OrderType.broadcastingInterval));
                        continue;
                    }
                    // serial_id
                    if (characteristicUuid.equals(OrderType.serialID.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.serialID, new MokoCharacteristic(characteristic, OrderType.serialID));
                        continue;
                    }
                    // iBeacon_name
                    if (characteristicUuid.equals(OrderType.iBeaconName.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.iBeaconName, new MokoCharacteristic(characteristic, OrderType.iBeaconName));
                        continue;
                    }
                    // connection_mode
                    if (characteristicUuid.equals(OrderType.connectionMode.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.connectionMode, new MokoCharacteristic(characteristic, OrderType.connectionMode));
                        continue;
                    }
                    // soft_reboot
                    if (characteristicUuid.equals(OrderType.softReboot.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.softReboot, new MokoCharacteristic(characteristic, OrderType.softReboot));
                        continue;
                    }
                    // iBeacon_mac
                    if (characteristicUuid.equals(OrderType.iBeaconMac.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.iBeaconMac, new MokoCharacteristic(characteristic, OrderType.iBeaconMac));
                        continue;
                    }
                    // overtime
                    if (characteristicUuid.equals(OrderType.overtime.getUuid())) {
                        mokoCharacteristicMap.put(OrderType.overtime, new MokoCharacteristic(characteristic, OrderType.overtime));
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

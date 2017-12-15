package com.moko.beaconsupport.handler;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.text.TextUtils;

import com.moko.beaconsupport.entity.BeaconCharacteristic;
import com.moko.beaconsupport.entity.OrderType;

import java.util.HashMap;
import java.util.List;

/**
 * @Date 2017/12/13 0013
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beaconsupport.handler.CharacteristicHandler
 */
public class CharacteristicHandler {
    private static CharacteristicHandler INSTANCE;

    public static final String SERVICE_UUID_HEADER_BATTERY = "0000180f";
    public static final String SERVICE_UUID_HEADER_SYSTEM = "0000180a";
    public static final String SERVICE_UUID_HEADER_PARAMS = "0000ff00";
    public HashMap<OrderType, BeaconCharacteristic> beaconCharacteristicMap;

    private CharacteristicHandler() {
        //no instance
        beaconCharacteristicMap = new HashMap<>();
    }

    public static CharacteristicHandler getInstance() {
        if (INSTANCE == null) {
            synchronized (CharacteristicHandler.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CharacteristicHandler();
                }
            }
        }
        return INSTANCE;
    }

    public HashMap<OrderType, BeaconCharacteristic> getCharacteristics(BluetoothGatt gatt) {
        if (beaconCharacteristicMap != null && !beaconCharacteristicMap.isEmpty()) {
            beaconCharacteristicMap.clear();
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
                        beaconCharacteristicMap.put(OrderType.battery, new BeaconCharacteristic(characteristic, OrderType.battery));
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
                    // 厂商名称
                    if (characteristicUuid.equals(OrderType.firmname.getUuid())) {
                        beaconCharacteristicMap.put(OrderType.firmname, new BeaconCharacteristic(characteristic, OrderType.firmname));
                        continue;
                    }
                    // 设备名称
                    if (characteristicUuid.equals(OrderType.devicename.getUuid())) {
                        beaconCharacteristicMap.put(OrderType.devicename, new BeaconCharacteristic(characteristic, OrderType.devicename));
                        continue;
                    }
                    // 出厂日期
                    if (characteristicUuid.equals(OrderType.iBeaconDate.getUuid())) {
                        beaconCharacteristicMap.put(OrderType.iBeaconDate, new BeaconCharacteristic(characteristic, OrderType.iBeaconDate));
                        continue;
                    }
                    // 硬件版本号
                    if (characteristicUuid.equals(OrderType.hardwareVersion.getUuid())) {
                        beaconCharacteristicMap.put(OrderType.hardwareVersion, new BeaconCharacteristic(characteristic, OrderType.hardwareVersion));
                        continue;
                    }
                    // 固件版本号
                    if (characteristicUuid.equals(OrderType.firmwareVersion.getUuid())) {
                        beaconCharacteristicMap.put(OrderType.firmwareVersion, new BeaconCharacteristic(characteristic, OrderType.firmwareVersion));
                        continue;
                    }
                    // 系统标示
                    if (characteristicUuid.equals(OrderType.systemMark.getUuid())) {
                        beaconCharacteristicMap.put(OrderType.systemMark, new BeaconCharacteristic(characteristic, OrderType.systemMark));
                        continue;
                    }
                    // IEEE标准信息
                    if (characteristicUuid.equals(OrderType.IEEEInfo.getUuid())) {
                        beaconCharacteristicMap.put(OrderType.IEEEInfo, new BeaconCharacteristic(characteristic, OrderType.IEEEInfo));
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
                    // 运行时间
                    if (characteristicUuid.equals(OrderType.runtimeAndChipModel.getUuid())) {
                        gatt.setCharacteristicNotification(characteristic, true);
                        beaconCharacteristicMap.put(OrderType.runtimeAndChipModel, new BeaconCharacteristic(characteristic, OrderType.runtimeAndChipModel));
                        continue;
                    }
                    // uuid
                    if (characteristicUuid.equals(OrderType.iBeaconUuid.getUuid())) {
                        beaconCharacteristicMap.put(OrderType.iBeaconUuid, new BeaconCharacteristic(characteristic, OrderType.iBeaconUuid));
                        continue;
                    }
                    // major
                    if (characteristicUuid.equals(OrderType.major.getUuid())) {
                        beaconCharacteristicMap.put(OrderType.major, new BeaconCharacteristic(characteristic, OrderType.major));
                        continue;
                    }
                    // minor
                    if (characteristicUuid.equals(OrderType.minor.getUuid())) {
                        beaconCharacteristicMap.put(OrderType.minor, new BeaconCharacteristic(characteristic, OrderType.minor));
                        continue;
                    }
                    // measure_power
                    if (characteristicUuid.equals(OrderType.measurePower.getUuid())) {
                        beaconCharacteristicMap.put(OrderType.measurePower, new BeaconCharacteristic(characteristic, OrderType.measurePower));
                        continue;
                    }
                    // transmission
                    if (characteristicUuid.equals(OrderType.transmission.getUuid())) {
                        beaconCharacteristicMap.put(OrderType.transmission, new BeaconCharacteristic(characteristic, OrderType.transmission));
                        continue;
                    }
                    // change_password
                    if (characteristicUuid.equals(OrderType.changePassword.getUuid())) {
                        gatt.setCharacteristicNotification(characteristic, true);
                        beaconCharacteristicMap.put(OrderType.changePassword, new BeaconCharacteristic(characteristic, OrderType.changePassword));
                        continue;
                    }
                    // broadcasting_interval
                    if (characteristicUuid.equals(OrderType.broadcastingInterval.getUuid())) {
                        beaconCharacteristicMap.put(OrderType.broadcastingInterval, new BeaconCharacteristic(characteristic, OrderType.broadcastingInterval));
                        continue;
                    }
                    // serial_id
                    if (characteristicUuid.equals(OrderType.serialID.getUuid())) {
                        beaconCharacteristicMap.put(OrderType.serialID, new BeaconCharacteristic(characteristic, OrderType.serialID));
                        continue;
                    }
                    // iBeacon_name
                    if (characteristicUuid.equals(OrderType.iBeaconName.getUuid())) {
                        beaconCharacteristicMap.put(OrderType.iBeaconName, new BeaconCharacteristic(characteristic, OrderType.iBeaconName));
                        continue;
                    }
                    // connection_mode
                    if (characteristicUuid.equals(OrderType.connectionMode.getUuid())) {
                        beaconCharacteristicMap.put(OrderType.connectionMode, new BeaconCharacteristic(characteristic, OrderType.connectionMode));
                        continue;
                    }
                    // soft_reboot
                    if (characteristicUuid.equals(OrderType.softReboot.getUuid())) {
                        beaconCharacteristicMap.put(OrderType.softReboot, new BeaconCharacteristic(characteristic, OrderType.softReboot));
                        continue;
                    }
                    // iBeacon_mac
                    if (characteristicUuid.equals(OrderType.iBeaconMac.getUuid())) {
                        beaconCharacteristicMap.put(OrderType.iBeaconMac, new BeaconCharacteristic(characteristic, OrderType.iBeaconMac));
                        continue;
                    }
                    // overtime
                    if (characteristicUuid.equals(OrderType.overtime.getUuid())) {
                        beaconCharacteristicMap.put(OrderType.overtime, new BeaconCharacteristic(characteristic, OrderType.overtime));
                        continue;
                    }
                }
            }
//            LogModule.i("service uuid:" + service.getUuid().toString());
//            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
//            for (BluetoothGattCharacteristic characteristic : characteristics) {
//                LogModule.i("characteristic uuid:" + characteristic.getUuid().toString());
//                LogModule.i("characteristic properties:" + Utils.getCharPropertie(characteristic.getProperties()));
//                List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
//                for (BluetoothGattDescriptor descriptor : descriptors) {
//                    LogModule.i("descriptor uuid:" + descriptor.getUuid().toString());
//                    LogModule.i("descriptor value:" + Utils.bytesToHexString(descriptor.getValue()));
//                }
//            }
        }
        return beaconCharacteristicMap;
    }
}

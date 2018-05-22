package com.moko.beacon.utils;

import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.SparseArray;

import com.moko.beacon.entity.BeaconInfo;
import com.moko.support.entity.DeviceInfo;
import com.moko.support.log.LogModule;
import com.moko.support.service.DeviceInfoParseable;
import com.moko.support.utils.MokoUtils;
import com.moko.support.utils.MokoUtils;

import java.text.DecimalFormat;
import java.util.Map;

import no.nordicsemi.android.support.v18.scanner.ScanResult;

/**
 * @Date 2018/1/10
 * @Author wenzheng.liu
 * @Description 通用解析工具类
 * @ClassPath com.moko.beacon.utils.BeaconInfoParseableImpl
 */
public class BeaconInfoParseableImpl implements DeviceInfoParseable<BeaconInfo> {

    @Override
    public BeaconInfo parseDeviceInfo(DeviceInfo deviceInfo) {
        ScanResult result = deviceInfo.scanResult;
        SparseArray<byte[]> manufacturer = result.getScanRecord().getManufacturerSpecificData();
        if (manufacturer == null || manufacturer.size() == 0) {
            return null;
        }
        String manufacturerSpecificData = MokoUtils.bytesToHexString(result.getScanRecord().getManufacturerSpecificData(manufacturer.keyAt(0)));
        if (TextUtils.isEmpty(manufacturerSpecificData) || !manufacturerSpecificData.startsWith("0215")) {
            return null;
        }
        // 0215fda50693a4e24fb1afcfc6eb0764782500000000c5
        // 0215e2c56db5dffb48d2b060d0f5a71096e000000000b0
        // LogModule.i("ManufacturerSpecificData:" + MokoUtils.bytesToHexString(result.getScanRecord().getManufacturerSpecificData(manufacturer.keyAt(0))));
        Map<ParcelUuid, byte[]> map = result.getScanRecord().getServiceData();
        if (map == null || map.isEmpty()) {
            return null;
        }
        String serviceDataUuid = null;
        String serviceData = null;
        for (ParcelUuid uuid : map.keySet()) {
            // 0000ff00-0000-1000-8000-00805f9b34fb
            // 0000ff01-0000-1000-8000-00805f9b34fb
            serviceDataUuid = uuid.getUuid().toString();
            if (!serviceDataUuid.startsWith("0000ff00") && !serviceDataUuid.startsWith("0000ff01")) {
                return null;
            }
            // 5e000000005080
            // 64000000005081
            serviceData = MokoUtils.bytesToHexString(result.getScanRecord().getServiceData(uuid));
            if (TextUtils.isEmpty(serviceData)) {
                return null;
            }
        }
        // uuid
        String hexString = manufacturerSpecificData.substring(4, 36);
        StringBuilder sb = new StringBuilder();
        sb.append(hexString.substring(0, 8));
        sb.append("-");
        sb.append(hexString.substring(8, 12));
        sb.append("-");
        sb.append(hexString.substring(12, 16));
        sb.append("-");
        sb.append(hexString.substring(16, 20));
        sb.append("-");
        sb.append(hexString.substring(20, 32));
        String uuid = sb.toString().toUpperCase();

        byte[] manufacturerSpecificDataByte = result.getScanRecord().getManufacturerSpecificData(manufacturer.keyAt(0));
        int major = (manufacturerSpecificDataByte[18] & 0xff) * 0x100 + (manufacturerSpecificDataByte[19] & 0xff);
        int minor = (manufacturerSpecificDataByte[20] & 0xff) * 0x100 + (manufacturerSpecificDataByte[21] & 0xff);
        int battery = Integer.parseInt(serviceData.substring(0, 2), 16);

        // 连接状态在版本号最高位，0不可连接，1可连接，判断后将版本号归位
        String versionStr = MokoUtils.hexString2binaryString(serviceData.substring(12, 14));
        // LogModule.i("version binary: " + versionStr);
        String connState = versionStr.substring(0, 1);
        boolean isConnected = Integer.parseInt(connState) == 1;
        String versionBinary = isConnected ? "0" + versionStr.substring(1, versionStr.length()) : versionStr;
        int version = Integer.parseInt(MokoUtils.binaryString2hexString(versionBinary), 16);
        // distance
        int acc = Integer.parseInt(serviceData.substring(10, 12), 16);
        String mac = deviceInfo.mac;
        double distance = MokoUtils.getDistance(deviceInfo.rssi, acc);
        String distanceDesc = "Unknown";
        if (distance <= 0.1) {
            distanceDesc = "Immediate";
        } else if (distance > 0.1 && distance <= 1.0) {
            distanceDesc = "Near";
        } else if (distance > 1.0) {
            distanceDesc = "Far";
        }
        // txPower;
        byte[] scanRecord = MokoUtils.hex2bytes(deviceInfo.scanRecord);
        // log
        String log = MokoUtils.bytesToHexString(scanRecord);
        int txPower = 0 - (int) scanRecord[32] & 0xff;

        // services
        String threeAxis = null;
        if (serviceDataUuid != null && serviceDataUuid.startsWith("0000ff01")) {
            byte[] threeAxisBytes = new byte[6];
            System.arraycopy(scanRecord, 44, threeAxisBytes, 0, 6);
            threeAxis = MokoUtils.bytesToHexString(threeAxisBytes).toUpperCase();
        }
        // ========================================================
        String distanceStr = new DecimalFormat("#0.00").format(distance);
        BeaconInfo beaconInfo = new BeaconInfo();
        beaconInfo.name = deviceInfo.name;
        beaconInfo.rssi = deviceInfo.rssi;
        beaconInfo.distance = distanceStr;
        beaconInfo.distanceDesc = distanceDesc;
        beaconInfo.major = major;
        beaconInfo.minor = minor;
        beaconInfo.txPower = txPower;
        beaconInfo.uuid = uuid;
        beaconInfo.batteryPower = battery;
        beaconInfo.version = version;
        beaconInfo.scanRecord = log;
        beaconInfo.isConnected = isConnected;
        beaconInfo.mac = mac;
        beaconInfo.threeAxis = threeAxis;
        return beaconInfo;
    }
}

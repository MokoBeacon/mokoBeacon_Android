package com.moko.beacon.utils;

import com.moko.beacon.entity.BeaconInfo;
import com.moko.support.entity.DeviceInfo;
import com.moko.support.utils.Utils;

import java.text.DecimalFormat;

/**
 * @Date 2018/1/10
 * @Author wenzheng.liu
 * @Description 通用解析工具类
 * @ClassPath com.moko.beacon.utils.CommonParseUtils
 */
public class CommonParseUtils {
    public static BeaconInfo parceDeviceInfo(DeviceInfo device) {
        byte[] scanRecord = Utils.hex2bytes(device.scanRecord);
        int startByte = 2;
        boolean patternFound = false;
        // iBeacon filter 0215
        while (startByte <= 5) {
            if (((int) scanRecord[startByte + 2] & 0xff) == 0x02
                    && ((int) scanRecord[startByte + 3] & 0xff) == 0x15) {
                // yes!  This is an iBeacon
                patternFound = true;
                break;
            }
            startByte++;
        }
        if (!patternFound) {
            // This is not an iBeacon
            return null;
        }
        // moko filter 00ff 01ff
        if (!((((int) scanRecord[startByte + 30] & 0xff) == 0x01 && ((int) scanRecord[startByte + 31] & 0xff) == 0xff)
                || (((int) scanRecord[startByte + 30] & 0xff) == 0x00 && ((int) scanRecord[startByte + 31] & 0xff) == 0xff))) {
            return null;
        }
        // log
        String log = Utils.bytesToHexString(scanRecord);
        // uuid
        byte[] proximityUuidBytes = new byte[16];
        System.arraycopy(scanRecord, startByte + 4, proximityUuidBytes, 0, 16);
        String hexString = Utils.bytesToHexString(proximityUuidBytes);
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
        String uuid = sb.toString();

        int major = (scanRecord[startByte + 20] & 0xff) * 0x100 + (scanRecord[startByte + 21] & 0xff);
        int minor = (scanRecord[startByte + 22] & 0xff) * 0x100 + (scanRecord[startByte + 23] & 0xff);
        int txPower = 0 - (int) scanRecord[startByte + 27] & 0xff;
        int battery = (int) scanRecord[startByte + 32] & 0xff;

        // 连接状态在版本号最高位，0不可连接，1可连接，判断后将版本号归位
        String versionStr = Utils.hexString2binaryString(Utils.byte2HexString(scanRecord[startByte + 38]));
//            LogModule.i("version binary: " + versionStr);
        String connState = versionStr.substring(0, 1);
        boolean isConnected = Integer.parseInt(connState) == 1;
        String versionBinary = isConnected ? "0" + versionStr.substring(1, versionStr.length()) : versionStr;
        int version = Integer.parseInt(Utils.binaryString2hexString(versionBinary), 16);
        // services
        int acc = (int) scanRecord[startByte + 37] & 0xff;
        String threeAxis = null;
        if (((int) scanRecord[startByte + 30] & 0xff) == 0x01
                && ((int) scanRecord[startByte + 31] & 0xff) == 0xff) {
            byte[] threeAxisBytes = new byte[6];
            System.arraycopy(scanRecord, startByte + 39, threeAxisBytes, 0, 6);
            threeAxis = Utils.bytesToHexString(threeAxisBytes).toUpperCase();
        }
        String mac = device.mac;
        double distance = Utils.getDistance(device.rssi, acc);
        String distanceDesc = "unknown";
        if (distance <= 0.1) {
            distanceDesc = "immediate";
        } else if (distance > 0.1 && distance <= 1.0) {
            distanceDesc = "near";
        } else if (distance > 1.0) {
            distanceDesc = "far";
        }
        // ========================================================
        String distanceStr = new DecimalFormat("#0.00").format(distance);
        BeaconInfo beaconInfo = new BeaconInfo();
        beaconInfo.name = device.name;
        beaconInfo.rssi = device.rssi;
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

package com.moko.beaconsupport.handler;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;

import com.moko.beaconsupport.callback.ScanDeviceCallback;
import com.moko.beaconsupport.entity.BeaconInfo;
import com.moko.beaconsupport.log.LogModule;
import com.moko.beaconsupport.utils.Utils;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @Date 2017/12/12 0012
 * @Author wenzheng.liu
 * @Description 搜索设备回调类
 * @ClassPath com.moko.beaconsupport.handler.BeaconLeScanHandler
 */
public class BeaconLeScanHandler implements BluetoothAdapter.LeScanCallback {
    private ScanDeviceCallback callback;
    private HashMap<String, BeaconInfo> beaconMap;

    public BeaconLeScanHandler(ScanDeviceCallback callback) {
        this.callback = callback;
        beaconMap = new HashMap<>();
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (device != null) {
            if (TextUtils.isEmpty(device.getName()) || scanRecord.length == 0 || rssi == 127) {
                return;
            }
            int startByte = 2;
            boolean patternFound = false;
            // 0215 00ff
            while (startByte <= 5) {
                if (((int) scanRecord[startByte + 2] & 0xff) == 0x02
                        && ((int) scanRecord[startByte + 3] & 0xff) == 0x15
                        && ((int) scanRecord[startByte + 30] & 0xff) == 0x00
                        && ((int) scanRecord[startByte + 31] & 0xff) == 0xff) {
                    // yes!  This is an iBeacon
                    patternFound = true;
                    break;
                }
                startByte++;
            }
            if (patternFound == false) {
                // This is not an iBeacon
                return;
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
            // distance acc
            int acc = (int) scanRecord[startByte + 37] & 0xff;
            // 连接状态在版本号最高位，0不可连接，1可连接，判断后将版本号归位
            String versionStr = Utils.hexString2binaryString(Utils.byte2HexString(scanRecord[startByte + 38]));
//            LogModule.i("version binary: " + versionStr);
            String connState = versionStr.substring(0, 1);
            boolean isConnected = Integer.parseInt(connState) == 1;
            String versionBinary = isConnected ? "0" + versionStr.substring(1, versionStr.length()) : versionStr;
            int version = Integer.parseInt(Utils.binaryString2hexString(versionBinary), 16);
            // ========================================================
            String mac = device.getAddress();
            double distance = Utils.getDistance(rssi, acc);
            String distanceDesc = "unknown";
            if (distance <= 0.1) {
                distanceDesc = "immediate";
            } else if (distance > 0.1 && distance <= 1.0) {
                distanceDesc = "near";
            } else if (distance > 1.0) {
                distanceDesc = "far";
            }
            String distanceStr = new DecimalFormat("#0.00").format(distance);
//            try {
//                Method isConnectedMethod = BluetoothDevice.class.getMethod("isConnected");
//                Boolean returnValue = (Boolean) isConnectedMethod.invoke(device);
//                LogModule.i("isConnected: " + returnValue.booleanValue());
//            } catch (Exception e) {
//            }
            if (!beaconMap.isEmpty() && beaconMap.containsKey(mac)) {
                BeaconInfo beaconInfo = beaconMap.get(mac);
                beaconInfo.name = device.getName();
                beaconInfo.rssi = rssi;
                beaconInfo.distance = distanceStr;
                beaconInfo.distanceDesc = distanceDesc;
                beaconInfo.major = major;
                beaconInfo.minor = minor;
                beaconInfo.txPower = txPower;
                beaconInfo.uuid = uuid;
                beaconInfo.batteryPower = battery;
                beaconInfo.version = version;
                beaconInfo.isConnected = isConnected;
                beaconInfo.scanRecord = log;
            } else {
                BeaconInfo beaconInfo = new BeaconInfo();
                beaconInfo.name = device.getName();
                beaconInfo.rssi = rssi;
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
                beaconMap.put(beaconInfo.mac, beaconInfo);
            }
            callback.onScanDevice(new ArrayList<>(beaconMap.values()));
        }
    }
}
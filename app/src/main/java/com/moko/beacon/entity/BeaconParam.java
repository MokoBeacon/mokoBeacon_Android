package com.moko.beacon.entity;

import java.io.Serializable;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beacon.entity.BeaconParam
 */
public class BeaconParam implements Serializable {
    // 设备电量
    public int battery;
    public String uuid;
    public int major;
    public int minor;
    // 校验距离
    public int measurePower;
    // 广播功率
    public int transmission;
    // 广播周期
    public int broadcastingInterval;
    // 设备ID
    public String serialID;
    // MAC地址
    public String iBeaconMAC;
    // 设备名称
    public String iBeaconName;
    // 连接模式
    public String connectionMode;
    // 系统信息
    public BeaconDeviceInfo beaconInfo;

    @Override
    public String toString() {
        return "BeaconParam{" +
                "battery=" + battery +
                ", uuid='" + uuid + '\'' +
                ", major=" + major +
                ", minor=" + minor +
                ", measurePower='" + measurePower + '\'' +
                ", transmission=" + transmission +
                ", broadcastingInterval=" + broadcastingInterval +
                ", serialID='" + serialID + '\'' +
                ", iBeaconMAC='" + iBeaconMAC + '\'' +
                ", iBeaconName='" + iBeaconName + '\'' +
                ", connectionMode='" + connectionMode + '\'' +
                ", beaconInfo=" + beaconInfo.toString() +
                '}';
    }
}

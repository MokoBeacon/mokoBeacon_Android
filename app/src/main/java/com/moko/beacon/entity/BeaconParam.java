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
    public String battery;
    public String uuid;
    public String major;
    public String minor;
    // 校验距离
    public String measurePower;
    // 广播功率
    public String transmission;
    // 广播周期
    public String broadcastingInterval;
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
    // 密码
    public String password;

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

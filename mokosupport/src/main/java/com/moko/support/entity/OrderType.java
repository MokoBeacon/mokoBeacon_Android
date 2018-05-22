package com.moko.support.entity;

import java.io.Serializable;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.entity.OrderType
 */
public enum OrderType implements Serializable {
    // 电池信息
    battery("battery", "00002a19-0000-1000-8000-00805f9b34fb"),
    // 软件版本
    softVersion("softVersion", "00002a28-0000-1000-8000-00805f9b34fb"),
    // 厂商名字
    firmname("firmname", "00002a29-0000-1000-8000-00805f9b34fb"),
    // 公司设备名
    devicename("devicename", "00002a24-0000-1000-8000-00805f9b34fb"),
    // 出厂时间
    iBeaconDate("iBeaconDate", "00002a25-0000-1000-8000-00805f9b34fb"),
    // 硬件版本
    hardwareVersion("hardwareVersion", "00002a27-0000-1000-8000-00805f9b34fb"),
    // 固件版本
    firmwareVersion("firmwareVersion", "00002a26-0000-1000-8000-00805f9b34fb"),
    // 带写和通知的特征
    writeAndNotify("writeAndNotify", "0000ffe0-0000-1000-8000-00805f9b34fb"),
    // UUID
    iBeaconUuid("iBeaconUuid", "0000ff01-0000-1000-8000-00805f9b34fb"),
    // major
    major("major", "0000ff02-0000-1000-8000-00805f9b34fb"),
    // minor
    minor("minor", "0000ff03-0000-1000-8000-00805f9b34fb"),
    // measure power
    measurePower("measurePower", "0000ff04-0000-1000-8000-00805f9b34fb"),
    // transmission
    transmission("transmission", "0000ff05-0000-1000-8000-00805f9b34fb"),
    // 修改密码
    changePassword("changePassword", "0000ff06-0000-1000-8000-00805f9b34fb"),
    // 广播间隔
    broadcastingInterval("broadcastingInterval", "0000ff07-0000-1000-8000-00805f9b34fb"),
    // serialID
    serialID("serialID", "0000ff08-0000-1000-8000-00805f9b34fb"),
    // iBeacon名称
    iBeaconName("iBeaconName", "0000ff09-0000-1000-8000-00805f9b34fb"),
    // 连接模式
    connectionMode("connectionMode", "0000ff0e-0000-1000-8000-00805f9b34fb"),
    // 重置
    softReboot("softReboot", "0000ff0f-0000-1000-8000-00805f9b34fb"),
    // iBeaconMac
    iBeaconMac("iBeaconMac", "0000ff0c-0000-1000-8000-00805f9b34fb"),
    // 超时时间
    overtime("overtime", "0000ff10-0000-1000-8000-00805f9b34fb");


    private String uuid;
    private String name;

    OrderType(String name, String uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }
}

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
    BATTERY("BATTERY", "00002a19-0000-1000-8000-00805f9b34fb"),
    // 软件版本
    SOFT_VERSION("SOFT_VERSION", "00002a28-0000-1000-8000-00805f9b34fb"),
    // 厂商名字
    MANUFACTURER("MANUFACTURER", "00002a29-0000-1000-8000-00805f9b34fb"),
    // 公司设备名
    DEVICE_MODEL("DEVICE_MODEL", "00002a24-0000-1000-8000-00805f9b34fb"),
    // 出厂时间
    PRODUCT_DATE("PRODUCT_DATE", "00002a25-0000-1000-8000-00805f9b34fb"),
    // 硬件版本
    HARDWARE_VERSION("HARDWARE_VERSION", "00002a27-0000-1000-8000-00805f9b34fb"),
    // 固件版本
    FIRMWARE_VERSION("FIRMWARE_VERSION", "00002a26-0000-1000-8000-00805f9b34fb"),
    // 带写和通知的特征
    PARAMS_CONFIG("PARAMS_CONFIG", "0000ffe0-0000-1000-8000-00805f9b34fb"),
    // UUID
    DEVICE_UUID("DEVICE_UUID", "0000ff01-0000-1000-8000-00805f9b34fb"),
    // major
    MAJOR("MAJOR", "0000ff02-0000-1000-8000-00805f9b34fb"),
    // minor
    MINOR("MINOR", "0000ff03-0000-1000-8000-00805f9b34fb"),
    // measure power
    MEASURE_POWER("MEASURE_POWER", "0000ff04-0000-1000-8000-00805f9b34fb"),
    // transmission
    TRANSMISSION("TRANSMISSION", "0000ff05-0000-1000-8000-00805f9b34fb"),
    // 修改密码
    PASSWORD("PASSWORD", "0000ff06-0000-1000-8000-00805f9b34fb"),
    // 广播间隔
    ADV_INTERVAL("ADV_INTERVAL", "0000ff07-0000-1000-8000-00805f9b34fb"),
    // serialID
    SERIAL_ID("SERIAL_ID", "0000ff08-0000-1000-8000-00805f9b34fb"),
    // iBeacon名称
    ADV_NAME("ADV_NAME", "0000ff09-0000-1000-8000-00805f9b34fb"),
    // 连接模式
    CONNECTION("CONNECTION", "0000ff0e-0000-1000-8000-00805f9b34fb"),
    // 重置
    SOFT_REBOOT("SOFT_REBOOT", "0000ff0f-0000-1000-8000-00805f9b34fb"),
    // iBeaconMac
    DEVICE_MAC("DEVICE_MAC", "0000ff0c-0000-1000-8000-00805f9b34fb"),
    // 超时时间
    OVER_TIME("OVER_TIME", "0000ff10-0000-1000-8000-00805f9b34fb");


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

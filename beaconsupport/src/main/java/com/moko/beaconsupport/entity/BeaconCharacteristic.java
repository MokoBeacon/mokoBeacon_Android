package com.moko.beaconsupport.entity;

import android.bluetooth.BluetoothGattCharacteristic;

import com.moko.beaconsupport.utils.Utils;

import java.io.Serializable;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beaconsupport.entity.BeaconCharacteristic
 */
public class BeaconCharacteristic implements Serializable {
    public BluetoothGattCharacteristic characteristic;
    public String charPropertie;
    public OrderType orderType;

    public BeaconCharacteristic(BluetoothGattCharacteristic characteristic, String charPropertie, OrderType orderType) {
        this.characteristic = characteristic;
        this.charPropertie = charPropertie;
        this.orderType = orderType;
    }

    public BeaconCharacteristic(BluetoothGattCharacteristic characteristic, OrderType orderType) {
        this(characteristic, Utils.getCharPropertie(characteristic.getProperties()), orderType);
    }
}

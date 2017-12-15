package com.moko.beaconsupport.callback;

/**
 * @Date 2017/12/12 0012
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beaconsupport.callback.BeaconResponseCallback
 */
public interface BeaconResponseCallback {

    void onCharacteristicChanged(byte[] value);

    void onCharacteristicWrite(byte[] value);

    void onCharacteristicRead(byte[] value);

    void onDescriptorWrite();
}

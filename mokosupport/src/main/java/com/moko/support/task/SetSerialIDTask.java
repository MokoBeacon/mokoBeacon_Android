package com.moko.support.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.entity.OrderCHAR;


public class SetSerialIDTask extends OrderTask {

    public byte[] data;

    public SetSerialIDTask() {
        super(OrderCHAR.CHAR_SERIAL_ID, RESPONSE_TYPE_WRITE);
    }

    public void setData(String deviceId) {
        data = deviceId.getBytes();
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

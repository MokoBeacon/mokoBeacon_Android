package com.moko.support.task;


import com.moko.ble.lib.task.OrderTask;
import com.moko.support.entity.OrderCHAR;

public class GetDeviceUUIDTask extends OrderTask {

    public byte[] data;

    public GetDeviceUUIDTask() {
        super(OrderCHAR.CHAR_DEVICE_UUID, RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

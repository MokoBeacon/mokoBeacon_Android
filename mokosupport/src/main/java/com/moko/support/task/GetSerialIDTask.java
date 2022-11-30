package com.moko.support.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.entity.OrderCHAR;

public class GetSerialIDTask extends OrderTask {

    public byte[] data;

    public GetSerialIDTask() {
        super(OrderCHAR.CHAR_SERIAL_ID, RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

package com.moko.support.task;


import com.moko.ble.lib.task.OrderTask;
import com.moko.support.entity.OrderCHAR;

public class GetTransmissionTask extends OrderTask {

    public byte[] data;

    public GetTransmissionTask() {
        super(OrderCHAR.CHAR_TRANSMISSION, RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

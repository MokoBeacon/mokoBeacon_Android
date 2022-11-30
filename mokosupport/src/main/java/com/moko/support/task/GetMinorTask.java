package com.moko.support.task;


import com.moko.ble.lib.task.OrderTask;
import com.moko.support.entity.OrderCHAR;

public class GetMinorTask extends OrderTask {

    public byte[] data;

    public GetMinorTask() {
        super(OrderCHAR.CHAR_MINOR, RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

package com.moko.support.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.entity.OrderCHAR;

public class GetMajorTask extends OrderTask {

    public byte[] data;

    public GetMajorTask() {
        super(OrderCHAR.CHAR_MAJOR, RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

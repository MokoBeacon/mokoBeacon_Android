package com.moko.support.task;


import com.moko.ble.lib.task.OrderTask;
import com.moko.support.entity.OrderCHAR;

public class GetMeasurePowerTask extends OrderTask {

    public byte[] data;

    public GetMeasurePowerTask() {
        super(OrderCHAR.CHAR_MEASURE_POWER, RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

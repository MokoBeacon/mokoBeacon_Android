package com.moko.support.task;


import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.entity.OrderCHAR;

public class SetMeasurePowerTask extends OrderTask {

    public byte[] data;

    public SetMeasurePowerTask() {
        super(OrderCHAR.CHAR_MEASURE_POWER, RESPONSE_TYPE_WRITE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(int measurePower) {
        data = MokoUtils.toByteArray(measurePower, 1);
    }
}

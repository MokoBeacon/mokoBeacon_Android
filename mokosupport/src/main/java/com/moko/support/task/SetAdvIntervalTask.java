package com.moko.support.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.entity.OrderCHAR;

public class SetAdvIntervalTask extends OrderTask {

    public byte[] data;

    public SetAdvIntervalTask() {
        super(OrderCHAR.CHAR_ADV_INTERVAL, RESPONSE_TYPE_WRITE);
    }

    public void setData(int advInterval) {
        data = MokoUtils.toByteArray(advInterval, 1);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

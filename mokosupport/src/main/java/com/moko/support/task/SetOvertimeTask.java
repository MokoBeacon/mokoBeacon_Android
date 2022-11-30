package com.moko.support.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.entity.OrderCHAR;

public class SetOvertimeTask extends OrderTask {

    public byte[] data;

    public SetOvertimeTask() {
        super(OrderCHAR.CHAR_OVER_TIME, RESPONSE_TYPE_WRITE);
    }

    public void setData() {
        data = MokoUtils.toByteArray(1, 1);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

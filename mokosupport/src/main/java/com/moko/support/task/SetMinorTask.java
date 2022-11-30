package com.moko.support.task;


import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.entity.OrderCHAR;

public class SetMinorTask extends OrderTask {

    public byte[] data;

    public SetMinorTask() {
        super(OrderCHAR.CHAR_MINOR, RESPONSE_TYPE_WRITE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(int minor) {
        data = MokoUtils.toByteArray(minor, 2);
    }
}

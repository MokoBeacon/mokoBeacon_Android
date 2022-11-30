package com.moko.support.task;


import com.moko.ble.lib.task.OrderTask;
import com.moko.support.entity.OrderCHAR;


public class SetSoftRebootTask extends OrderTask {

    public byte[] data;

    public SetSoftRebootTask() {
        super(OrderCHAR.CHAR_SOFT_REBOOT, RESPONSE_TYPE_WRITE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

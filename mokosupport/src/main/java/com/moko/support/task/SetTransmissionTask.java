package com.moko.support.task;


import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.entity.OrderCHAR;

public class SetTransmissionTask extends OrderTask {

    public byte[] data;

    public SetTransmissionTask() {
        super(OrderCHAR.CHAR_TRANSMISSION, RESPONSE_TYPE_WRITE);
    }

    public void setData(int transmission) {
        data = MokoUtils.toByteArray(transmission, 1);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

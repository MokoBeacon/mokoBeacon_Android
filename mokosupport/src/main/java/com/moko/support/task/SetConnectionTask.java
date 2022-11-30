package com.moko.support.task;


import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.entity.OrderCHAR;

public class SetConnectionTask extends OrderTask {

    public byte[] data;

    public SetConnectionTask() {
        super(OrderCHAR.CHAR_CONNECTION, RESPONSE_TYPE_WRITE);
    }

    public void setData(String connectionMode) {
        data = MokoUtils.hex2bytes(connectionMode);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

package com.moko.support.task;


import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.entity.OrderCHAR;


public class GetConnectionTask extends OrderTask {

    public byte[] data;

    public GetConnectionTask() {
        super(OrderCHAR.CHAR_CONNECTION, RESPONSE_TYPE_READ);
    }

    public void setData(String connectionMode) {
        data = MokoUtils.hex2bytes(connectionMode);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

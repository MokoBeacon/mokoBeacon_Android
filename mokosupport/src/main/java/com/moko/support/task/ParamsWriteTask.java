package com.moko.support.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.entity.OrderCHAR;
import com.moko.support.entity.ParamsKeyEnum;

public class ParamsWriteTask extends OrderTask {
    public byte[] data;

    public ParamsWriteTask() {
        super(OrderCHAR.CHAR_PARAMS, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setAxisEnable(boolean enable) {
        data = new byte[]{
                (byte) 0xEA,
                (byte) ParamsKeyEnum.GET_THREE_AXES.getParamsKey(),
                (byte) 0x00,
                (byte) 0x01,
                (byte) (enable ? 0 : 1),
        };
    }
    public void close() {
        data = new byte[]{
                (byte) 0xEA,
                (byte) ParamsKeyEnum.SET_CLOSE.getParamsKey(),
                (byte) 0x00,
                (byte) 0x01,
                (byte) 0x00,
        };
    }
}

package com.moko.support.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.entity.OrderCHAR;

public class SetPasswordTask extends OrderTask {

    public byte[] data;

    public SetPasswordTask() {
        super(OrderCHAR.CHAR_PASSWORD, RESPONSE_TYPE_WRITE_NO_RESPONSE);
    }

    public void setData(String password) {
        data = password.getBytes();
        response.responseValue = data;
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

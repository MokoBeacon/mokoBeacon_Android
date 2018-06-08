package com.moko.support.task;

import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.entity.OrderType;
import com.moko.support.utils.MokoUtils;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.ChangePasswordTask
 */
public class ChangePasswordTask extends OrderTask {

    public byte[] data;

    public ChangePasswordTask(MokoOrderTaskCallback callback, int sendDataType) {
        super(OrderType.changePassword, callback, sendDataType);
    }

    public void setData(String password) {
        String passwordHex = MokoUtils.string2Hex(password);
        data = MokoUtils.hex2bytes(passwordHex);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

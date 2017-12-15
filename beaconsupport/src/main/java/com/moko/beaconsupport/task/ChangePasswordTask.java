package com.moko.beaconsupport.task;

import com.moko.beaconsupport.callback.OrderTaskCallback;
import com.moko.beaconsupport.entity.OrderType;
import com.moko.beaconsupport.utils.Utils;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beaconsupport.task.ChangePasswordTask
 */
public class ChangePasswordTask extends OrderTask {

    public byte[] data;

    public ChangePasswordTask(OrderTaskCallback callback, int sendDataType) {
        super(OrderType.changePassword, callback, sendDataType);
    }

    public void setData(String password) {
        String passwordHex = Utils.string2Hex(password);
        data = Utils.hex2bytes(passwordHex);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

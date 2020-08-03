package com.moko.support.task;

import com.moko.support.entity.OrderType;
import com.moko.support.utils.MokoUtils;

/**
 * @Date 2020/8/3
 * @Author wenzheng.liu
 * @Description 
 * @ClassPath com.moko.support.task.SetPassword
 */
public class SetPassword extends OrderTask {

    public byte[] data;

    public SetPassword() {
        super(OrderType.PASSWORD, RESPONSE_TYPE_WRITE_NO_RESPONSE);
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

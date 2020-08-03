package com.moko.support.task;

import com.moko.support.entity.OrderType;
import com.moko.support.utils.MokoUtils;

/**
 * @Date 2020/8/3
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.SetMajor
 */
public class SetMajor extends OrderTask {

    public byte[] data;

    public SetMajor() {
        super(OrderType.MAJOR, RESPONSE_TYPE_WRITE);
    }

    public void setData(int marjor) {
        byte[] marjorBytes = MokoUtils.hex2bytes(Integer.toHexString(marjor));
        if (marjorBytes.length < 2) {
            data = new byte[2];
            data[0] = 0;
            data[1] = marjorBytes[0];
        } else {
            data = marjorBytes;
        }
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

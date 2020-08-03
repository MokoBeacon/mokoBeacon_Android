package com.moko.support.task;


import com.moko.support.entity.OrderType;
import com.moko.support.utils.MokoUtils;

/**
 * @Date 2020/8/3
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.SetMinor
 */
public class SetMinor extends OrderTask {

    public byte[] data;

    public SetMinor() {
        super(OrderType.MINOR, RESPONSE_TYPE_WRITE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(int minor) {
        byte[] minorBytes = MokoUtils.hex2bytes(Integer.toHexString(minor));
        if (minorBytes.length < 2) {
            data = new byte[2];
            data[0] = 0;
            data[1] = minorBytes[0];
        } else {
            data = minorBytes;
        }
    }
}

package com.moko.support.task;


import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.entity.OrderType;
import com.moko.support.utils.MokoUtils;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.MinorTask
 */
public class MinorTask extends OrderTask {

    public byte[] data;

    public MinorTask(MokoOrderTaskCallback callback, int sendDataType) {
        super(OrderType.minor, callback, sendDataType);
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

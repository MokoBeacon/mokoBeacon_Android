package com.moko.support.task;

import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.entity.OrderType;
import com.moko.support.utils.MokoUtils;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.MajorTask
 */
public class MajorTask extends OrderTask {

    public byte[] data;

    public MajorTask(MokoOrderTaskCallback callback, int sendDataType) {
        super(OrderType.major, callback, sendDataType);
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

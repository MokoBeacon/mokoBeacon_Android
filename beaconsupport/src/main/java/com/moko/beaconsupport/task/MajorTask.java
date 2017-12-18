package com.moko.beaconsupport.task;

import com.moko.beaconsupport.callback.OrderTaskCallback;
import com.moko.beaconsupport.entity.OrderType;
import com.moko.beaconsupport.utils.Utils;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beaconsupport.task.MajorTask
 */
public class MajorTask extends OrderTask {

    public byte[] data;

    public MajorTask(OrderTaskCallback callback, int sendDataType) {
        super(OrderType.major, callback, sendDataType);
    }

    public void setData(int marjor) {
        byte[] marjorBytes = Utils.hex2bytes(Integer.toHexString(marjor));
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

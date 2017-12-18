package com.moko.beaconsupport.task;

import com.moko.beaconsupport.callback.OrderTaskCallback;
import com.moko.beaconsupport.entity.OrderType;
import com.moko.beaconsupport.utils.Utils;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beaconsupport.task.MinorTask
 */
public class MinorTask extends OrderTask {

    public byte[] data;

    public MinorTask(OrderTaskCallback callback, int sendDataType) {
        super(OrderType.minor, callback, sendDataType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(int minor) {
        byte[] minorBytes = Utils.hex2bytes(Integer.toHexString(minor));
        if (minorBytes.length < 2) {
            data = new byte[2];
            data[0] = 0;
            data[1] = minorBytes[0];
        } else {
            data = minorBytes;
        }
    }
}

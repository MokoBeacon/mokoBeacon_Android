package com.moko.beaconsupport.task;

import com.moko.beaconsupport.callback.OrderTaskCallback;
import com.moko.beaconsupport.entity.OrderType;

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

    public MajorTask setData(int marjor) {
        data = new byte[1];
        data[0] = (byte) marjor;
        return this;
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

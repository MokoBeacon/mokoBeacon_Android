package com.moko.beaconsupport.task;

import com.moko.beaconsupport.callback.OrderTaskCallback;
import com.moko.beaconsupport.entity.OrderType;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beaconsupport.task.ChipModelTask
 */
public class ChipModelTask extends OrderTask {

    public byte[] data;

    public ChipModelTask(OrderTaskCallback callback, int sendDataType) {
        super(OrderType.runtimeAndChipModel, callback, sendDataType);
        setData();
    }

    public void setData() {
        data = new byte[4];
        data[0] = Integer.valueOf(Integer.toHexString(234), 16).byteValue();
        data[1] = (byte) 91;
        data[2] = 0;
        data[3] = 0;
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

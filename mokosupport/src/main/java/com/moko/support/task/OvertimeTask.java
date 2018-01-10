package com.moko.support.task;

import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.entity.OrderType;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.IBeaconMacTask
 */
public class OvertimeTask extends OrderTask {

    public byte[] data;

    public OvertimeTask(MokoOrderTaskCallback callback, int sendDataType) {
        super(OrderType.overtime, callback, sendDataType);
        setData();
    }

    public void setData() {
        data = new byte[1];
        data[0] = (byte) 1;
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

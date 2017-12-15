package com.moko.beaconsupport.task;

import com.moko.beaconsupport.callback.OrderTaskCallback;
import com.moko.beaconsupport.entity.OrderType;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beaconsupport.task.IBeaconMacTask
 */
public class OvertimeTask extends OrderTask {

    public byte[] data;

    public OvertimeTask(OrderTaskCallback callback, int sendDataType) {
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

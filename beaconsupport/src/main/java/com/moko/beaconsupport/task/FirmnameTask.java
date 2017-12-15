package com.moko.beaconsupport.task;

import com.moko.beaconsupport.callback.OrderTaskCallback;
import com.moko.beaconsupport.entity.OrderType;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beaconsupport.task.FirmnameTask
 */
public class FirmnameTask extends OrderTask {

    public byte[] data;

    public FirmnameTask(OrderTaskCallback callback, int sendDataType) {
        super(OrderType.firmname, callback, sendDataType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

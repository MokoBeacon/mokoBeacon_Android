package com.moko.beaconsupport.task;

import com.moko.beaconsupport.callback.OrderTaskCallback;
import com.moko.beaconsupport.entity.OrderType;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beaconsupport.task.BroadcastingIntervalTask
 */
public class BroadcastingIntervalTask extends OrderTask {

    public byte[] data;

    public BroadcastingIntervalTask(OrderTaskCallback callback, int sendDataType) {
        super(OrderType.broadcastingInterval, callback, sendDataType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

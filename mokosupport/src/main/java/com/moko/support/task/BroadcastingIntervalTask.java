package com.moko.support.task;

import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.entity.OrderType;
import com.moko.support.utils.MokoUtils;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.BroadcastingIntervalTask
 */
public class BroadcastingIntervalTask extends OrderTask {

    public byte[] data;

    public BroadcastingIntervalTask(MokoOrderTaskCallback callback, int sendDataType) {
        super(OrderType.broadcastingInterval, callback, sendDataType);
    }

    public void setData(int broadcastInterval) {
        data = MokoUtils.hex2bytes(Integer.toHexString(broadcastInterval));
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

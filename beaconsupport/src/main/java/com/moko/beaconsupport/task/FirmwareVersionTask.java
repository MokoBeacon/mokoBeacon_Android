package com.moko.beaconsupport.task;

import com.moko.beaconsupport.callback.OrderTaskCallback;
import com.moko.beaconsupport.entity.OrderType;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beaconsupport.task.FirmwareVersionTask
 */
public class FirmwareVersionTask extends OrderTask {

    public byte[] data;

    public FirmwareVersionTask(OrderTaskCallback callback, int sendDataType) {
        super(OrderType.firmwareVersion, callback, sendDataType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

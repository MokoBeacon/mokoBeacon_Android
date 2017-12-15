package com.moko.beaconsupport.task;

import com.moko.beaconsupport.callback.OrderTaskCallback;
import com.moko.beaconsupport.entity.OrderType;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beaconsupport.task.SoftRebootModeTask
 */
public class SoftRebootModeTask extends OrderTask {

    public byte[] data;

    public SoftRebootModeTask(OrderTaskCallback callback, int sendDataType) {
        super(OrderType.softReboot, callback, sendDataType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

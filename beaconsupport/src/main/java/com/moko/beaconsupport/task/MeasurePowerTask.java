package com.moko.beaconsupport.task;

import com.moko.beaconsupport.callback.OrderTaskCallback;
import com.moko.beaconsupport.entity.OrderType;
import com.moko.beaconsupport.utils.Utils;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beaconsupport.task.MeasurePowerTask
 */
public class MeasurePowerTask extends OrderTask {

    public byte[] data;

    public MeasurePowerTask(OrderTaskCallback callback, int sendDataType) {
        super(OrderType.measurePower, callback, sendDataType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(int measurePower) {
        data = Utils.hex2bytes(Integer.toHexString(measurePower));
    }
}

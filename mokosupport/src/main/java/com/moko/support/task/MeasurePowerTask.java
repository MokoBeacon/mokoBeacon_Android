package com.moko.support.task;


import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.entity.OrderType;
import com.moko.support.utils.MokoUtils;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.MeasurePowerTask
 */
public class MeasurePowerTask extends OrderTask {

    public byte[] data;

    public MeasurePowerTask(MokoOrderTaskCallback callback, int sendDataType) {
        super(OrderType.measurePower, callback, sendDataType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(int measurePower) {
        data = MokoUtils.hex2bytes(Integer.toHexString(measurePower));
    }
}

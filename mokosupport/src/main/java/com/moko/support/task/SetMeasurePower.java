package com.moko.support.task;


import com.moko.support.entity.OrderType;
import com.moko.support.utils.MokoUtils;

/**
 * @Date 2020/8/3
 * @Author wenzheng.liu
 * @Description 
 * @ClassPath com.moko.support.task.SetMeasurePower
 */
public class SetMeasurePower extends OrderTask {

    public byte[] data;

    public SetMeasurePower() {
        super(OrderType.MEASURE_POWER, RESPONSE_TYPE_WRITE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(int measurePower) {
        data = MokoUtils.hex2bytes(Integer.toHexString(measurePower));
    }
}

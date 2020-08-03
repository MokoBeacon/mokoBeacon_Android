package com.moko.support.task;


import com.moko.support.entity.OrderType;
import com.moko.support.utils.MokoUtils;

/**
 * @Date 2020/8/3
 * @Author wenzheng.liu
 * @Description 
 * @ClassPath com.moko.support.task.GetMeasurePower
 */
public class GetMeasurePower extends OrderTask {

    public byte[] data;

    public GetMeasurePower() {
        super(OrderType.MEASURE_POWER, RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

package com.moko.support.task;

import com.moko.support.entity.OrderType;

/**
 * @Date 2020/8/3
 * @Author wenzheng.liu
 * @Description 
 * @ClassPath com.moko.support.task.GetBattery
 */
public class GetBattery extends OrderTask {

    public byte[] data;

    public GetBattery() {
        super(OrderType.BATTERY, RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

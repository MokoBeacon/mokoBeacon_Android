package com.moko.support.task;

import com.moko.support.entity.OrderType;

/**
 * @Date 2020/8/3
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.GetSerialID
 */
public class GetSerialID extends OrderTask {

    public byte[] data;

    public GetSerialID() {
        super(OrderType.SERIAL_ID, RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

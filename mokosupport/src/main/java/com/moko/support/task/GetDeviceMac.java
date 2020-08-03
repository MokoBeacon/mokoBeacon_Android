package com.moko.support.task;


import com.moko.support.entity.OrderType;

/**
 * @Date 2020/8/3
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.GetDeviceMac
 */
public class GetDeviceMac extends OrderTask {

    public byte[] data;

    public GetDeviceMac() {
        super(OrderType.DEVICE_MAC, RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

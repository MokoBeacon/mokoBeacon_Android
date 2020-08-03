package com.moko.support.task;


import com.moko.support.entity.OrderType;

/**
 * @Date 2020/8/3
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.GetDeviceModel
 */
public class GetDeviceModel extends OrderTask {

    public byte[] data;

    public GetDeviceModel() {
        super(OrderType.DEVICE_MODEL, RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

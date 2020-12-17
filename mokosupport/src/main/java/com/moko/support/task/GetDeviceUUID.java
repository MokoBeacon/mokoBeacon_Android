package com.moko.support.task;


import com.moko.support.entity.OrderType;

/**
 * @Date 2020/8/3
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.GetDeviceUUID
 */
public class GetDeviceUUID extends OrderTask {

    public byte[] data;

    public GetDeviceUUID() {
        super(OrderType.DEVICE_UUID, RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

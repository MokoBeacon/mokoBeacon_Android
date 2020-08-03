package com.moko.support.task;


import com.moko.support.entity.OrderType;

/**
 * @Date 2020/8/3
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.GetHardwareVersion
 */
public class GetHardwareVersion extends OrderTask {

    public byte[] data;

    public GetHardwareVersion() {
        super(OrderType.HARDWARE_VERSION, RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

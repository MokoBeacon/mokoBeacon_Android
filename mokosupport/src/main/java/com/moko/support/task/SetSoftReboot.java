package com.moko.support.task;


import com.moko.support.entity.OrderType;

/**
 * @Date 2020/8/3
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.SetSoftReboot
 */
public class SetSoftReboot extends OrderTask {

    public byte[] data;

    public SetSoftReboot() {
        super(OrderType.SOFT_REBOOT, RESPONSE_TYPE_WRITE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

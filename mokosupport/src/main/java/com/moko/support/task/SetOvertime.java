package com.moko.support.task;

import com.moko.support.entity.OrderType;

/**
 * @Date 2020/8/3
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.SetOvertime
 */
public class SetOvertime extends OrderTask {

    public byte[] data;

    public SetOvertime() {
        super(OrderType.OVER_TIME, RESPONSE_TYPE_WRITE);
    }

    public void setData() {
        data = new byte[1];
        data[0] = (byte) 1;
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

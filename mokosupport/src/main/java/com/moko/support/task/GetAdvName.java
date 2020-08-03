package com.moko.support.task;


import com.moko.support.entity.OrderType;

/**
 * @Date 2020/8/3
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.GetAdvName
 */
public class GetAdvName extends OrderTask {

    public byte[] data;

    public GetAdvName() {
        super(OrderType.ADV_NAME, RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

package com.moko.support.task;


import com.moko.support.entity.OrderType;

/**
 * @Date 2020/8/3
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.GetProductDate
 */
public class GetProductDate extends OrderTask {

    public byte[] data;

    public GetProductDate() {
        super(OrderType.PRODUCT_DATE, RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

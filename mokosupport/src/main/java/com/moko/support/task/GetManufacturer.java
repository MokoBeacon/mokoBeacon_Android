package com.moko.support.task;


import com.moko.support.entity.OrderType;

/**
 * @Date 2020/8/3
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.GetManufacturer
 */
public class GetManufacturer extends OrderTask {

    public byte[] data;

    public GetManufacturer() {
        super(OrderType.MANUFACTURER, RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

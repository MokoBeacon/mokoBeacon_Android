package com.moko.support.task;


import com.moko.support.entity.OrderType;
import com.moko.support.utils.MokoUtils;

/**
 * @Date 2020/8/3
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.GetTransmission
 */
public class GetTransmission extends OrderTask {

    public byte[] data;

    public GetTransmission() {
        super(OrderType.TRANSMISSION, RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

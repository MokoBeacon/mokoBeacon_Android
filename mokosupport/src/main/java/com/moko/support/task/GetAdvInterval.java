package com.moko.support.task;

import com.moko.support.entity.OrderType;
import com.moko.support.utils.MokoUtils;

/**
 * @Date 2020/8/3
 * @Author wenzheng.liu
 * @Description 
 * @ClassPath com.moko.support.task.GetAdvInterval
 */
public class GetAdvInterval extends OrderTask {

    public byte[] data;

    public GetAdvInterval() {
        super(OrderType.ADV_INTERVAL, RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

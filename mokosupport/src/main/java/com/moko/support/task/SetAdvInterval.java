package com.moko.support.task;

import com.moko.support.entity.OrderType;
import com.moko.support.utils.MokoUtils;

/**
 * @Date 2020/8/3
 * @Author wenzheng.liu
 * @Description 
 * @ClassPath com.moko.support.task.SetAdvInterval
 */
public class SetAdvInterval extends OrderTask {

    public byte[] data;

    public SetAdvInterval() {
        super(OrderType.ADV_INTERVAL, RESPONSE_TYPE_WRITE);
    }

    public void setData(int advInterval) {
        data = MokoUtils.hex2bytes(Integer.toHexString(advInterval));
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

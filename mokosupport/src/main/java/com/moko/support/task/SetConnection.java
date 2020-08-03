package com.moko.support.task;


import com.moko.support.entity.OrderType;
import com.moko.support.utils.MokoUtils;

/**
 * @Date 2020/8/3
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.SetConnection
 */
public class SetConnection extends OrderTask {

    public byte[] data;

    public SetConnection() {
        super(OrderType.CONNECTION, RESPONSE_TYPE_WRITE);
    }

    public void setData(String connectionMode) {
        data = MokoUtils.hex2bytes(connectionMode);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

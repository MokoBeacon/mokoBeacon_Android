package com.moko.support.task;


import com.moko.support.entity.OrderType;
import com.moko.support.utils.MokoUtils;

/**
 * @Date 2020/8/3
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.GetConnection
 */
public class GetConnection extends OrderTask {

    public byte[] data;

    public GetConnection() {
        super(OrderType.CONNECTION, RESPONSE_TYPE_READ);
    }

    public void setData(String connectionMode) {
        data = MokoUtils.hex2bytes(connectionMode);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

package com.moko.support.task;

import com.moko.support.entity.OrderType;
import com.moko.support.utils.MokoUtils;

/**
 * @Date 2020/8/3
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.SetSerialID
 */
public class SetSerialID extends OrderTask {

    public byte[] data;

    public SetSerialID() {
        super(OrderType.SERIAL_ID, RESPONSE_TYPE_WRITE);
    }

    public void setData(String deviceId) {
        data = MokoUtils.hex2bytes(MokoUtils.string2Hex(deviceId));
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

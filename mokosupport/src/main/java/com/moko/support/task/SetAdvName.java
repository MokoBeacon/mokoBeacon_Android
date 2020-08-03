package com.moko.support.task;


import com.moko.support.entity.OrderType;
import com.moko.support.utils.MokoUtils;

/**
 * @Date 2020/8/3
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.SetAdvName
 */
public class SetAdvName extends OrderTask {

    public byte[] data;

    public SetAdvName() {
        super(OrderType.ADV_NAME, RESPONSE_TYPE_WRITE);
    }

    public void setData(String deviceName) {
        data = MokoUtils.hex2bytes(MokoUtils.string2Hex(deviceName));
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

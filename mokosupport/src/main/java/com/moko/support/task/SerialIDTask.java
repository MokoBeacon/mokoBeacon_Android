package com.moko.support.task;

import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.entity.OrderType;
import com.moko.support.utils.MokoUtils;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.SerialIDTask
 */
public class SerialIDTask extends OrderTask {

    public byte[] data;

    public SerialIDTask(MokoOrderTaskCallback callback, int sendDataType) {
        super(OrderType.serialID, callback, sendDataType);
    }

    public void setData(String deviceId) {
        data = MokoUtils.hex2bytes(MokoUtils.string2Hex(deviceId));
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

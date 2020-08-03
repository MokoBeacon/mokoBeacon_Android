package com.moko.support.task;


import com.moko.support.entity.OrderType;
import com.moko.support.utils.MokoUtils;

/**
 * @Date 2020/8/3
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.SetDeviceUUID
 */
public class SetDeviceUUID extends OrderTask {

    public byte[] data;

    public SetDeviceUUID() {
        super(OrderType.DEVICE_UUID, RESPONSE_TYPE_WRITE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(String uuid) {
        String uuidHex = uuid.replaceAll("-", "");
        data = MokoUtils.hex2bytes(uuidHex);
    }
}

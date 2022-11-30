package com.moko.support.task;


import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.entity.OrderCHAR;

public class SetDeviceUUIDTask extends OrderTask {

    public byte[] data;

    public SetDeviceUUIDTask() {
        super(OrderCHAR.CHAR_DEVICE_UUID, RESPONSE_TYPE_WRITE);
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

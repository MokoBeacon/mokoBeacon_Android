package com.moko.support.task;


import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.entity.OrderType;
import com.moko.support.utils.MokoUtils;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.IBeaconUuidTask
 */
public class IBeaconUuidTask extends OrderTask {

    public byte[] data;

    public IBeaconUuidTask(MokoOrderTaskCallback callback, int sendDataType) {
        super(OrderType.iBeaconUuid, callback, sendDataType);
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

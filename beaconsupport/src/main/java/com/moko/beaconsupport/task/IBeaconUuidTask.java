package com.moko.beaconsupport.task;

import com.moko.beaconsupport.callback.OrderTaskCallback;
import com.moko.beaconsupport.entity.OrderType;
import com.moko.beaconsupport.utils.Utils;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beaconsupport.task.IBeaconUuidTask
 */
public class IBeaconUuidTask extends OrderTask {

    public byte[] data;

    public IBeaconUuidTask(OrderTaskCallback callback, int sendDataType) {
        super(OrderType.iBeaconUuid, callback, sendDataType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(String uuid) {
        String uuidHex = uuid.replaceAll("-", "");
        data = Utils.hex2bytes(uuidHex);
    }
}

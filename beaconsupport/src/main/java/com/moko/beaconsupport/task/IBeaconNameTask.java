package com.moko.beaconsupport.task;

import com.moko.beaconsupport.callback.OrderTaskCallback;
import com.moko.beaconsupport.entity.OrderType;
import com.moko.beaconsupport.utils.Utils;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beaconsupport.task.IBeaconNameTask
 */
public class IBeaconNameTask extends OrderTask {

    public byte[] data;

    public IBeaconNameTask(OrderTaskCallback callback, int sendDataType) {
        super(OrderType.iBeaconName, callback, sendDataType);
    }
    public void setData(String deviceName) {
        data = Utils.hex2bytes(Utils.string2Hex(deviceName));
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

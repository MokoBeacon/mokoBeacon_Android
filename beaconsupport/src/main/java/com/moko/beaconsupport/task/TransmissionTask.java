package com.moko.beaconsupport.task;

import com.moko.beaconsupport.callback.OrderTaskCallback;
import com.moko.beaconsupport.entity.OrderType;
import com.moko.beaconsupport.utils.Utils;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beaconsupport.task.TransmissionTask
 */
public class TransmissionTask extends OrderTask {

    public byte[] data;

    public TransmissionTask(OrderTaskCallback callback, int sendDataType) {
        super(OrderType.transmission, callback, sendDataType);
    }

    public void setData(int transmission) {
        data = Utils.hex2bytes(Integer.toHexString(transmission));
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

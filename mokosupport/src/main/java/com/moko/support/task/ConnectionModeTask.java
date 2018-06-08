package com.moko.support.task;


import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.entity.OrderType;
import com.moko.support.utils.MokoUtils;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.ConnectionModeTask
 */
public class ConnectionModeTask extends OrderTask {

    public byte[] data;

    public ConnectionModeTask(MokoOrderTaskCallback callback, int sendDataType) {
        super(OrderType.connectionMode, callback, sendDataType);
    }

    public void setData(String connectionMode) {
        data = MokoUtils.hex2bytes(connectionMode);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

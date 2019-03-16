package com.moko.support.task;


import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.entity.OrderType;

/**
 * @Date 2019/3/12
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.CloseTask
 */
public class CloseTask extends OrderTask {

    public byte[] data;

    public CloseTask(MokoOrderTaskCallback callback, int sendDataType) {
        super(OrderType.writeAndNotify, callback, sendDataType);
        setData();
    }

    public void setData() {
        data = new byte[5];
        data[0] = (byte) 0xEA;
        data[1] = (byte) 0x6D;
        data[2] = 0;
        data[3] = 1;
        data[4] = 0;
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

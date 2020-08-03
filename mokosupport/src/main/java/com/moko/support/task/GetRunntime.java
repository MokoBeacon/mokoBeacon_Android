package com.moko.support.task;


import com.moko.support.entity.ConfigKeyEnum;
import com.moko.support.entity.OrderType;

/**
 * @Date 2020/8/3
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.GetRunntime
 */
public class GetRunntime extends OrderTask {

    public byte[] data;

    public GetRunntime() {
        super(OrderType.PARAMS_CONFIG, RESPONSE_TYPE_WRITE_NO_RESPONSE);
        setData();
    }

    public void setData() {
        data = new byte[4];
        data[0] = (byte) 0xEA;
        data[1] = (byte) ConfigKeyEnum.GET_RUNNTIME.getConfigKey();
        data[2] = 0;
        data[3] = 0;
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

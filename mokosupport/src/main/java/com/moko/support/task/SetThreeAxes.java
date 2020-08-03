package com.moko.support.task;


import com.moko.support.entity.ConfigKeyEnum;
import com.moko.support.entity.OrderType;

/**
 * @Date 2020/8/3
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.GetThreeAxes
 */
public class SetThreeAxes extends OrderTask {

    public byte[] data;

    public SetThreeAxes() {
        super(OrderType.PARAMS_CONFIG, RESPONSE_TYPE_WRITE_NO_RESPONSE);
    }

    public void setData(boolean isOpen) {
        data = new byte[5];
        data[0] = (byte) 0xEA;
        data[1] = (byte) ConfigKeyEnum.GET_THREE_AXES.getConfigKey();
        data[2] = 0;
        data[3] = 1;
        data[4] = (byte) (isOpen ? 0 : 1);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}

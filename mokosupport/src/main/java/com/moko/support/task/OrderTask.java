package com.moko.support.task;

import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.entity.OrderType;

/**
 * @Date 2018/1/10
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.OrderTask
 */
public abstract class OrderTask {
    public static final int RESPONSE_TYPE_READ = 0;
    public static final int RESPONSE_TYPE_WRITE = 1;
    public static final int RESPONSE_TYPE_NOTIFY = 2;
    public static final int RESPONSE_TYPE_WRITE_NO_RESPONSE = 3;
    public static final int ORDER_STATUS_SUCCESS = 1;
    public OrderType orderType;
    public MokoOrderTaskCallback mokoOrderTaskCallback;
    public int responseType;
    public int orderStatus;


    public OrderTask(OrderType orderType, MokoOrderTaskCallback callback, int responseType) {
        this.orderType = orderType;
        this.mokoOrderTaskCallback = callback;
        this.responseType = responseType;
    }

    public abstract byte[] assemble();
}

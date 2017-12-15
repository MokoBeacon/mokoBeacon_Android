package com.moko.beaconsupport.task;

import com.moko.beaconsupport.callback.OrderTaskCallback;
import com.moko.beaconsupport.entity.OrderType;

/**
 * @Date 2017/12/14 0014
 * @Author wenzheng.liu
 * @Description 命令任务
 */
public abstract class OrderTask {
    public static final int RESPONSE_TYPE_READ = 0;
    public static final int RESPONSE_TYPE_WRITE = 1;
    public static final int RESPONSE_TYPE_NOTIFY = 2;
    public static final int ORDER_STATUS_SUCCESS = 1;
    public OrderType orderType;
    public OrderTaskCallback orderTaskCallback;
    public int responseType;
    public int orderStatus;


    public OrderTask(OrderType orderType,OrderTaskCallback callback,int responseType) {
        this.orderType = orderType;
        this.orderTaskCallback = callback;
        this.responseType = responseType;
    }

    public abstract byte[] assemble();
}

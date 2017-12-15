package com.moko.beaconsupport.callback;

import com.moko.beaconsupport.entity.OrderType;

/**
 * @Date 2017/5/10
 * @Author wenzheng.liu
 * @Description 返回数据回调类
 * @ClassPath com.fitpolo.support.callback.OrderCallback
 */
public interface OrderTaskCallback {

    void onOrderResult(OrderType orderType, byte[] value);

    void onOrderTimeout(OrderType orderType);

    void onOrderFinish();
}

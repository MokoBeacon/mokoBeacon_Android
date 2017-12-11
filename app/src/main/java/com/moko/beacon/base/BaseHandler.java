package com.moko.beacon.base;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * @Date 2017/12/8 0008
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beacon.base.BaseHandler
 */
public abstract class BaseHandler<T> extends Handler {
    private WeakReference<T> reference;

    public BaseHandler(T t) {
        super(Looper.getMainLooper());
        reference = new WeakReference<>(t);
    }

    @Override
    public void handleMessage(Message msg) {
        if (reference.get() == null) {
            return;
        }
        handleMessage(reference.get(), msg);
    }

    protected abstract void handleMessage(T t, Message msg);
}

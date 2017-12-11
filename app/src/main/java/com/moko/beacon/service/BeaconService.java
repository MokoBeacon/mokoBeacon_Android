package com.moko.beacon.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.moko.beaconsupport.beacon.BeaconModule;
import com.moko.beaconsupport.log.LogModule;

/**
 * @Date 2017/12/7 0007
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beacon.service.BeaconService
 */
public class BeaconService extends Service {
    private IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public BeaconService getService() {
            return BeaconService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        BeaconModule.getInstance().init(this);
        LogModule.i("后台服务创建");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogModule.i("后台服务启动");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        LogModule.i("后台服务关闭");
        super.onDestroy();
    }
}

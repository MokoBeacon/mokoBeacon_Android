package com.moko.beacon;

import android.app.Application;
import android.content.Intent;

import com.moko.beacon.service.BeaconService;
import com.moko.beaconsupport.beacon.BeaconModule;

/**
 * @Date 2017/12/7 0007
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beacon.BaseApplication
 */
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        BeaconModule.getInstance().init(getApplicationContext());
        // 启动蓝牙服务
        startService(new Intent(this, BeaconService.class));
    }
}

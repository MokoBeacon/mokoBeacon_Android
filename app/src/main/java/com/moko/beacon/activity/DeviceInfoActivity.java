package com.moko.beacon.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.View;

import com.moko.beacon.BeaconConstants;
import com.moko.beacon.R;
import com.moko.beacon.service.BeaconService;
import com.moko.beaconsupport.beacon.BeaconModule;

import butterknife.OnClick;

/**
 * @Date 2017/12/13 0013
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beacon.activity.DeviceInfoActivity
 */
public class DeviceInfoActivity extends Activity {

    private BeaconService mBeaconService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);
        bindService(new Intent(this, BeaconService.class), mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        unbindService(mServiceConnection);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                abortBroadcast();
                String action = intent.getAction();
                if (BeaconConstants.ACTION_CONNECT_SUCCESS.equals(action)) {
                }
                if (BeaconConstants.ACTION_CONNECT_DISCONNECTED.equals(action)) {

                }
            }
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBeaconService = ((BeaconService.LocalBinder) service).getService();
            // 注册广播接收器
            IntentFilter filter = new IntentFilter();
            filter.addAction(BeaconConstants.ACTION_CONNECT_SUCCESS);
            filter.addAction(BeaconConstants.ACTION_CONNECT_DISCONNECTED);
            filter.setPriority(200);
            registerReceiver(mReceiver, filter);
            if (!BeaconModule.getInstance().isBluetoothOpen()) {
                // 蓝牙未打开，开启蓝牙
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, BeaconConstants.REQUEST_CODE_ENABLE_BT);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @OnClick({R.id.tv_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                finish();
                break;
        }
    }
}

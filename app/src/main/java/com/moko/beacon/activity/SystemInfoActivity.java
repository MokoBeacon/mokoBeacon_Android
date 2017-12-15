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
import android.widget.TextView;

import com.moko.beacon.BeaconConstants;
import com.moko.beacon.R;
import com.moko.beacon.entity.BeaconDeviceInfo;
import com.moko.beacon.service.BeaconService;
import com.moko.beacon.utils.ToastUtils;
import com.moko.beaconsupport.beacon.BeaconModule;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @Date 2017/12/15 0015
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beacon.activity.SystemInfoActivity
 */
public class SystemInfoActivity extends Activity {
    @Bind(R.id.tv_ibeacon_firmname)
    TextView tvIbeaconFirmname;
    @Bind(R.id.tv_ibeacon_device_name)
    TextView tvIbeaconDeviceName;
    @Bind(R.id.tv_ibeacon_date)
    TextView tvIbeaconDate;
    @Bind(R.id.tv_ibeacon_mac)
    TextView tvIbeaconMac;
    @Bind(R.id.tv_ibeacon_chip_mode)
    TextView tvIbeaconChipMode;
    @Bind(R.id.tv_ibeacon_hardware_version)
    TextView tvIbeaconHardwareVersion;
    @Bind(R.id.tv_ibeacon_firmware_version)
    TextView tvIbeaconFirmwareVersion;
    @Bind(R.id.tv_ibeacon_runtime)
    TextView tvIbeaconRuntime;
    @Bind(R.id.tv_ibeacon_system_mark)
    TextView tvIbeaconSystemMark;
    @Bind(R.id.tv_ibeacon_ieee_info)
    TextView tvIbeaconIeeeInfo;
    private BeaconService mBeaconService;
    private BeaconDeviceInfo mBeaconDeviceInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_info);
        ButterKnife.bind(this);
        bindService(new Intent(this, BeaconService.class), mServiceConnection, BIND_AUTO_CREATE);
        mBeaconDeviceInfo = (BeaconDeviceInfo) getIntent().getSerializableExtra(BeaconConstants.EXTRA_KEY_DEVICE_INFO);
        if (mBeaconDeviceInfo == null) {
            finish();
            return;
        }
        tvIbeaconFirmname.setText(mBeaconDeviceInfo.firmname);
        tvIbeaconDeviceName.setText(mBeaconDeviceInfo.deviceName);
        tvIbeaconDate.setText(mBeaconDeviceInfo.iBeaconDate);
        tvIbeaconMac.setText(mBeaconDeviceInfo.iBeaconMac);
        tvIbeaconChipMode.setText(mBeaconDeviceInfo.chipModel);
        tvIbeaconHardwareVersion.setText(mBeaconDeviceInfo.hardwareVersion);
        tvIbeaconFirmwareVersion.setText(mBeaconDeviceInfo.firmwareVersion);
        tvIbeaconRuntime.setText(mBeaconDeviceInfo.runtime);
        tvIbeaconSystemMark.setText(mBeaconDeviceInfo.systemMark);
        tvIbeaconIeeeInfo.setText(mBeaconDeviceInfo.IEEEInfo);
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
                String action = intent.getAction();
                if (BeaconConstants.ACTION_CONNECT_DISCONNECTED.equals(action)) {
                    ToastUtils.showToast(SystemInfoActivity.this, "设备断开连接");
                    finish();
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
            filter.addAction(BeaconConstants.ACTION_CONNECT_DISCONNECTED);
            filter.setPriority(300);
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

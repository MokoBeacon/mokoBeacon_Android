package com.moko.beacon.activity;

import android.app.Activity;
import android.app.ProgressDialog;
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
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.moko.beacon.BeaconConstants;
import com.moko.beacon.R;
import com.moko.beacon.entity.BeaconParam;
import com.moko.beacon.service.BeaconService;
import com.moko.beacon.utils.ToastUtils;
import com.moko.beaconsupport.beacon.BeaconModule;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @Date 2017/12/13 0013
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beacon.activity.DeviceInfoActivity
 */
public class DeviceInfoActivity extends Activity {
    @Bind(R.id.tv_conn_state)
    TextView tvConnState;
    @Bind(R.id.tv_ibeacon_battery)
    TextView tvIbeaconBattery;
    @Bind(R.id.tv_ibeacon_uuid)
    TextView tvIbeaconUuid;
    @Bind(R.id.tv_ibeacon_major)
    TextView tvIbeaconMajor;
    @Bind(R.id.tv_ibeacon_minor)
    TextView tvIbeaconMinor;
    @Bind(R.id.tv_ibeacon_measure_power)
    TextView tvIbeaconMeasurePower;
    @Bind(R.id.tv_ibeacon_transmission)
    TextView tvIbeaconTransmission;
    @Bind(R.id.tv_ibeacon_broadcasting_interval)
    TextView tvIbeaconBroadcastingInterval;
    @Bind(R.id.tv_ibeacon_serialID)
    TextView tvIbeaconSerialID;
    @Bind(R.id.tv_ibeacon_mac)
    TextView tvIbeaconMac;
    @Bind(R.id.tv_ibeacon_device_name)
    TextView tvIbeaconDeviceName;
    @Bind(R.id.tv_ibeacon_device_conn_mode)
    TextView tvIbeaconDeviceConnMode;
    private BeaconService mBeaconService;
    private BeaconParam mBeaconParam;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);
        ButterKnife.bind(this);
        bindService(new Intent(this, BeaconService.class), mServiceConnection, BIND_AUTO_CREATE);
        mBeaconParam = (BeaconParam) getIntent().getSerializableExtra(BeaconConstants.EXTRA_KEY_DEVICE_PARAM);
        if (mBeaconParam == null) {
            finish();
            return;
        }
        if (BeaconModule.getInstance().isConnDevice(this, mBeaconParam.iBeaconMAC)) {
            tvConnState.setText(getString(R.string.device_info_conn_status_connected));
        } else {
            tvConnState.setText(getString(R.string.device_info_conn_status_disconnect));
        }
        tvIbeaconBattery.setText(mBeaconParam.battery + "");
        tvIbeaconUuid.setText(mBeaconParam.uuid);
        tvIbeaconMajor.setText(mBeaconParam.major + "");
        tvIbeaconMinor.setText(mBeaconParam.minor + "");
        tvIbeaconMeasurePower.setText(String.format("-%ddBm", mBeaconParam.measurePower));
        tvIbeaconTransmission.setText(mBeaconParam.transmission + "");
        tvIbeaconBroadcastingInterval.setText(mBeaconParam.broadcastingInterval + "");
        tvIbeaconSerialID.setText(mBeaconParam.serialID);
        tvIbeaconMac.setText(mBeaconParam.iBeaconMAC);
        tvIbeaconDeviceName.setText(mBeaconParam.iBeaconName);
        tvIbeaconDeviceConnMode.setText("00".equals(mBeaconParam.connectionMode) ? "YES" : "NO");
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
                    tvConnState.setText(getString(R.string.device_info_conn_status_connected));
                    mBeaconService.sendOrder(mBeaconService.setOvertime());
                    dismissLoadingProgressDialog();
                    ToastUtils.showToast(DeviceInfoActivity.this, "Connect Success");
                }
                if (BeaconConstants.ACTION_CONNECT_DISCONNECTED.equals(action)) {
                    tvConnState.setText(getString(R.string.device_info_conn_status_disconnect));
                    dismissLoadingProgressDialog();
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
            filter.addAction(BeaconConstants.ACTION_RESPONSE_SUCCESS);
            filter.addAction(BeaconConstants.ACTION_RESPONSE_TIMEOUT);
            filter.addAction(BeaconConstants.ACTION_RESPONSE_FINISH);
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            back();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void back() {
        if (BeaconModule.getInstance().isConnDevice(this, mBeaconParam.iBeaconMAC)) {
            BeaconModule.getInstance().disConnectBle();
        }
        finish();
    }


    @OnClick({R.id.tv_back, R.id.tv_conn_state, R.id.rl_ibeacon_battery, R.id.rl_ibeacon_uuid, R.id.rl_ibeacon_major, R.id.rl_ibeacon_minor, R.id.rl_ibeacon_measure_power, R.id.rl_ibeacon_transmission, R.id.rl_ibeacon_broadcasting_interval, R.id.rl_ibeacon_serialID, R.id.rl_ibeacon_mac, R.id.rl_ibeacon_device_name, R.id.rl_ibeacon_device_conn_mode, R.id.rl_ibeacon_change_password, R.id.rl_ibeacon_device_info})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                back();
                break;
            case R.id.tv_conn_state:
                if (!BeaconModule.getInstance().isConnDevice(this, mBeaconParam.iBeaconMAC)) {
                    mBeaconService.connDevice(mBeaconParam.iBeaconMAC);
                    showLoadingProgressDialog();
                }
                break;
            case R.id.rl_ibeacon_uuid:
                if (!BeaconModule.getInstance().isConnDevice(this, mBeaconParam.iBeaconMAC)) {
                    ToastUtils.showToast(this, "设备连接断开，请点击右上角按钮重连");
                    return;
                }
                break;
            case R.id.rl_ibeacon_major:
                if (!BeaconModule.getInstance().isConnDevice(this, mBeaconParam.iBeaconMAC)) {
                    ToastUtils.showToast(this, "设备连接断开，请点击右上角按钮重连");
                    return;
                }
                break;
            case R.id.rl_ibeacon_minor:
                if (!BeaconModule.getInstance().isConnDevice(this, mBeaconParam.iBeaconMAC)) {
                    ToastUtils.showToast(this, "设备连接断开，请点击右上角按钮重连");
                    return;
                }
                break;
            case R.id.rl_ibeacon_measure_power:
                if (!BeaconModule.getInstance().isConnDevice(this, mBeaconParam.iBeaconMAC)) {
                    ToastUtils.showToast(this, "设备连接断开，请点击右上角按钮重连");
                    return;
                }
                break;
            case R.id.rl_ibeacon_transmission:
                if (!BeaconModule.getInstance().isConnDevice(this, mBeaconParam.iBeaconMAC)) {
                    ToastUtils.showToast(this, "设备连接断开，请点击右上角按钮重连");
                    return;
                }
                break;
            case R.id.rl_ibeacon_broadcasting_interval:
                if (!BeaconModule.getInstance().isConnDevice(this, mBeaconParam.iBeaconMAC)) {
                    ToastUtils.showToast(this, "设备连接断开，请点击右上角按钮重连");
                    return;
                }
                break;
            case R.id.rl_ibeacon_serialID:
                if (!BeaconModule.getInstance().isConnDevice(this, mBeaconParam.iBeaconMAC)) {
                    ToastUtils.showToast(this, "设备连接断开，请点击右上角按钮重连");
                    return;
                }
                break;
            case R.id.rl_ibeacon_device_name:
                if (!BeaconModule.getInstance().isConnDevice(this, mBeaconParam.iBeaconMAC)) {
                    ToastUtils.showToast(this, "设备连接断开，请点击右上角按钮重连");
                    return;
                }
                break;
            case R.id.rl_ibeacon_device_conn_mode:
                if (!BeaconModule.getInstance().isConnDevice(this, mBeaconParam.iBeaconMAC)) {
                    ToastUtils.showToast(this, "设备连接断开，请点击右上角按钮重连");
                    return;
                }
                break;
            case R.id.rl_ibeacon_change_password:
                if (!BeaconModule.getInstance().isConnDevice(this, mBeaconParam.iBeaconMAC)) {
                    ToastUtils.showToast(this, "设备连接断开，请点击右上角按钮重连");
                    return;
                }
                break;
            case R.id.rl_ibeacon_device_info:
                if (!BeaconModule.getInstance().isConnDevice(this, mBeaconParam.iBeaconMAC)) {
                    ToastUtils.showToast(this, "设备连接断开，请点击右上角按钮重连");
                    return;
                }
                Intent intent = new Intent(this, SystemInfoActivity.class);
                intent.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_INFO, mBeaconParam.beaconInfo);
                startActivity(intent);
                break;
        }
    }

    private ProgressDialog mLoadingDialog;

    private void showLoadingProgressDialog() {
        mLoadingDialog = new ProgressDialog(DeviceInfoActivity.this);
        mLoadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mLoadingDialog.setCanceledOnTouchOutside(false);
        mLoadingDialog.setCancelable(false);
        mLoadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mLoadingDialog.setMessage("连接中...");
        if (!isFinishing() && mLoadingDialog != null && !mLoadingDialog.isShowing()) {
            mLoadingDialog.show();
        }
    }

    private void dismissLoadingProgressDialog() {
        if (!isFinishing() && mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }
}

package com.moko.beacon.activity;

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
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.moko.beacon.BeaconConstants;
import com.moko.beacon.R;
import com.moko.beacon.entity.BeaconDeviceInfo;
import com.moko.beacon.service.BeaconService;
import com.moko.beacon.utils.ToastUtils;
import com.moko.beaconsupport.beacon.BeaconModule;
import com.moko.beaconsupport.entity.OrderType;
import com.moko.beaconsupport.task.OrderTask;
import com.moko.beaconsupport.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @Date 2017/12/15 0015
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beacon.activity.SystemInfoActivity
 */
public class SystemInfoActivity extends BaseActivity {
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
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        if (TextUtils.isEmpty(mBeaconDeviceInfo.firmname)) {
            orderTasks.add(mBeaconService.getFirmname());
        } else {
            tvIbeaconFirmname.setText(mBeaconDeviceInfo.firmname);
        }
        if (TextUtils.isEmpty(mBeaconDeviceInfo.deviceName)) {
            orderTasks.add(mBeaconService.getDevicename());
        } else {
            tvIbeaconDeviceName.setText(mBeaconDeviceInfo.deviceName);
        }
        if (TextUtils.isEmpty(mBeaconDeviceInfo.iBeaconDate)) {
            orderTasks.add(mBeaconService.getiBeaconDate());
        } else {
            tvIbeaconDate.setText(mBeaconDeviceInfo.iBeaconDate);
        }
        if (TextUtils.isEmpty(mBeaconDeviceInfo.iBeaconMac)) {
            orderTasks.add(mBeaconService.getIBeaconMac());
        } else {
            tvIbeaconMac.setText(mBeaconDeviceInfo.iBeaconMac);
        }
        if (TextUtils.isEmpty(mBeaconDeviceInfo.chipModel)) {
            orderTasks.add(mBeaconService.getChipModel());
        } else {
            tvIbeaconChipMode.setText(mBeaconDeviceInfo.chipModel);
        }
        if (TextUtils.isEmpty(mBeaconDeviceInfo.hardwareVersion)) {
            orderTasks.add(mBeaconService.getChipModel());
        } else {
            tvIbeaconHardwareVersion.setText(mBeaconDeviceInfo.hardwareVersion);
        }
        if (TextUtils.isEmpty(mBeaconDeviceInfo.firmwareVersion)) {
            orderTasks.add(mBeaconService.getChipModel());
        } else {
            tvIbeaconFirmwareVersion.setText(mBeaconDeviceInfo.firmwareVersion);
        }
        if (TextUtils.isEmpty(mBeaconDeviceInfo.runtime)) {
            orderTasks.add(mBeaconService.getChipModel());
        } else {
            tvIbeaconRuntime.setText(mBeaconDeviceInfo.runtime);
        }
        if (TextUtils.isEmpty(mBeaconDeviceInfo.systemMark)) {
            orderTasks.add(mBeaconService.getChipModel());
        } else {
            tvIbeaconSystemMark.setText(mBeaconDeviceInfo.systemMark);
        }
        if (TextUtils.isEmpty(mBeaconDeviceInfo.IEEEInfo)) {
            orderTasks.add(mBeaconService.getChipModel());
        } else {
            tvIbeaconIeeeInfo.setText(mBeaconDeviceInfo.IEEEInfo);
        }
        if (!orderTasks.isEmpty()) {
            showLoadingProgressDialog();
            for (OrderTask ordertask : orderTasks) {
                mBeaconService.sendOrder(ordertask);
            }
        }
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
                    ToastUtils.showToast(SystemInfoActivity.this, getString(R.string.alert_diconnected));
                    finish();
                }
                if (BeaconConstants.ACTION_RESPONSE_FINISH.equals(action)) {
                    mBeaconService.mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dismissLoadingProgressDialog();
                        }
                    }, 1000);

                }
                if (BeaconConstants.ACTION_RESPONSE_SUCCESS.equals(action)) {
                    OrderType orderType = (OrderType) intent.getSerializableExtra(BeaconConstants.EXTRA_KEY_RESPONSE_ORDER_TYPE);
                    byte[] value = intent.getByteArrayExtra(BeaconConstants.EXTRA_KEY_RESPONSE_VALUE);
                    switch (orderType) {
                        case iBeaconMac:
                            String hexMac = Utils.bytesToHexString(value);
                            if (hexMac.length() > 11) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(hexMac.substring(0, 2));
                                sb.append(":");
                                sb.append(hexMac.substring(2, 4));
                                sb.append(":");
                                sb.append(hexMac.substring(4, 6));
                                sb.append(":");
                                sb.append(hexMac.substring(6, 8));
                                sb.append(":");
                                sb.append(hexMac.substring(8, 10));
                                sb.append(":");
                                sb.append(hexMac.substring(10, 12));
                                String mac = sb.toString().toUpperCase();
                                mBeaconDeviceInfo.iBeaconMac = mac;
                                tvIbeaconMac.setText(mBeaconDeviceInfo.iBeaconMac);
                            }
                            break;
                        case firmname:
                            mBeaconDeviceInfo.firmname = Utils.hex2String(Utils.bytesToHexString(value));
                            tvIbeaconFirmname.setText(mBeaconDeviceInfo.firmname);
                            break;
                        case devicename:
                            mBeaconDeviceInfo.deviceName = Utils.hex2String(Utils.bytesToHexString(value));
                            tvIbeaconDeviceName.setText(mBeaconDeviceInfo.deviceName);
                            break;
                        case iBeaconDate:
                            mBeaconDeviceInfo.iBeaconDate = Utils.hex2String(Utils.bytesToHexString(value));
                            tvIbeaconDate.setText(mBeaconDeviceInfo.iBeaconDate);
                            break;
                        case hardwareVersion:
                            mBeaconDeviceInfo.hardwareVersion = Utils.hex2String(Utils.bytesToHexString(value));
                            tvIbeaconHardwareVersion.setText(mBeaconDeviceInfo.hardwareVersion);
                            break;
                        case firmwareVersion:
                            mBeaconDeviceInfo.firmwareVersion = Utils.hex2String(Utils.bytesToHexString(value));
                            tvIbeaconFirmwareVersion.setText(mBeaconDeviceInfo.firmwareVersion);
                            break;
                        case runtimeAndChipModel:
                            if ("0004".equals(Utils.bytesToHexString(Arrays.copyOfRange(value, 0, 2)))) {
                                byte[] runtimeBytes = Arrays.copyOfRange(value, 2, value.length);
                                int runtime = Integer.parseInt(Utils.bytesToHexString(runtimeBytes), 16);
                                int runtimeDays = runtime / (60 * 60 * 24);
                                int runtimeHours = (runtime % (60 * 60 * 24)) / (60 * 60);
                                int runtimeMinutes = (runtime % (60 * 60)) / (60);
                                int runtimeSeconds = (runtime % (60)) / 1000;
                                mBeaconDeviceInfo.runtime = String.format("%dD%dh%dm%ds", runtimeDays, runtimeHours, runtimeMinutes, runtimeSeconds);
                                tvIbeaconRuntime.setText(mBeaconDeviceInfo.runtime);
                            }
                            if ("000c".equals(Utils.bytesToHexString(Arrays.copyOfRange(value, 0, 2)).toLowerCase())) {
                                byte[] chipModelBytes = Arrays.copyOfRange(value, 2, value.length);
                                mBeaconDeviceInfo.chipModel = Utils.hex2String(Utils.bytesToHexString(chipModelBytes));
                                tvIbeaconChipMode.setText(mBeaconDeviceInfo.chipModel);
                            }
                            break;
                        case systemMark:
                            mBeaconDeviceInfo.systemMark = Utils.bytesToHexString(value);
                            tvIbeaconSystemMark.setText(mBeaconDeviceInfo.systemMark);
                        case IEEEInfo:
                            mBeaconDeviceInfo.IEEEInfo = Utils.bytesToHexString(value);
                            tvIbeaconIeeeInfo.setText(mBeaconDeviceInfo.IEEEInfo);
                            break;
                    }
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
                back();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            back();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void back() {
        Intent intent = new Intent();
        intent.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_INFO, mBeaconDeviceInfo);
        setResult(RESULT_OK);
        finish();
    }

    private ProgressDialog mLoadingDialog;

    private void showLoadingProgressDialog() {
        mLoadingDialog = new ProgressDialog(SystemInfoActivity.this);
        mLoadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mLoadingDialog.setCanceledOnTouchOutside(false);
        mLoadingDialog.setCancelable(false);
        mLoadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mLoadingDialog.setMessage("Syncing...");
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

package com.moko.beacon.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.moko.beacon.BeaconConstants;
import com.moko.beacon.R;
import com.moko.beacon.entity.BeaconDeviceInfo;
import com.moko.beacon.utils.ToastUtils;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.OrderTaskAssembler;
import com.moko.support.entity.OrderType;
import com.moko.support.event.ConnectStatusEvent;
import com.moko.support.event.OrderTaskResponseEvent;
import com.moko.support.task.OrderTask;
import com.moko.support.task.OrderTaskResponse;
import com.moko.support.utils.MokoUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @Date 2017/12/15 0015
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beacon.activity.SystemInfoActivity
 */
public class SystemInfoActivity extends BaseActivity {
    @BindView(R.id.tv_ibeacon_soft_version)
    TextView tvIbeaconSoftVersion;
    @BindView(R.id.tv_ibeacon_firmname)
    TextView tvIbeaconFirmname;
    @BindView(R.id.tv_ibeacon_device_name)
    TextView tvIbeaconDeviceName;
    @BindView(R.id.tv_ibeacon_date)
    TextView tvIbeaconDate;
    @BindView(R.id.tv_ibeacon_mac)
    TextView tvIbeaconMac;
    @BindView(R.id.tv_ibeacon_chip_mode)
    TextView tvIbeaconChipMode;
    @BindView(R.id.tv_ibeacon_hardware_version)
    TextView tvIbeaconHardwareVersion;
    @BindView(R.id.tv_ibeacon_firmware_version)
    TextView tvIbeaconFirmwareVersion;
    @BindView(R.id.tv_ibeacon_runtime)
    TextView tvIbeaconRuntime;
    private BeaconDeviceInfo mBeaconDeviceInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_info);
        ButterKnife.bind(this);
        mBeaconDeviceInfo = (BeaconDeviceInfo) getIntent().getSerializableExtra(BeaconConstants.EXTRA_KEY_DEVICE_INFO);
        if (mBeaconDeviceInfo == null) {
            finish();
            return;
        }
        EventBus.getDefault().register(this);
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        if (TextUtils.isEmpty(mBeaconDeviceInfo.softVersion)) {
            orderTasks.add(OrderTaskAssembler.getSoftVersion());
        } else {
            tvIbeaconSoftVersion.setText(mBeaconDeviceInfo.softVersion);
        }
        if (TextUtils.isEmpty(mBeaconDeviceInfo.firmname)) {
            orderTasks.add(OrderTaskAssembler.getDeviceModel());
        } else {
            tvIbeaconFirmname.setText(mBeaconDeviceInfo.firmname);
        }
        if (TextUtils.isEmpty(mBeaconDeviceInfo.deviceName)) {
            orderTasks.add(OrderTaskAssembler.getManufacturer());
        } else {
            tvIbeaconDeviceName.setText(mBeaconDeviceInfo.deviceName);
        }
        if (TextUtils.isEmpty(mBeaconDeviceInfo.iBeaconDate)) {
            orderTasks.add(OrderTaskAssembler.getProductDate());
        } else {
            tvIbeaconDate.setText(mBeaconDeviceInfo.iBeaconDate);
        }
        if (TextUtils.isEmpty(mBeaconDeviceInfo.iBeaconMac)) {
            orderTasks.add(OrderTaskAssembler.getDeviceMac());
        } else {
            tvIbeaconMac.setText(mBeaconDeviceInfo.iBeaconMac);
        }
        if (TextUtils.isEmpty(mBeaconDeviceInfo.chipModel)) {
            orderTasks.add(OrderTaskAssembler.getChipModel());
        } else {
            tvIbeaconChipMode.setText(mBeaconDeviceInfo.chipModel);
        }
        if (TextUtils.isEmpty(mBeaconDeviceInfo.hardwareVersion)) {
            orderTasks.add(OrderTaskAssembler.getHardwareVersion());
        } else {
            tvIbeaconHardwareVersion.setText(mBeaconDeviceInfo.hardwareVersion);
        }
        if (TextUtils.isEmpty(mBeaconDeviceInfo.firmwareVersion)) {
            orderTasks.add(OrderTaskAssembler.getFirmwareVersion());
        } else {
            tvIbeaconFirmwareVersion.setText(mBeaconDeviceInfo.firmwareVersion);
        }
        if (TextUtils.isEmpty(mBeaconDeviceInfo.runtime)) {
            orderTasks.add(OrderTaskAssembler.getRunntime());
        } else {
            tvIbeaconRuntime.setText(mBeaconDeviceInfo.runtime);
        }
        if (!orderTasks.isEmpty()) {
            if (!MokoSupport.getInstance().isBluetoothOpen()) {
                ToastUtils.showToast(this, "bluetooth is closed,please open");
                return;
            }
            showLoadingProgressDialog();
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_CONN_STATUS_DISCONNECTED.equals(action)) {
                ToastUtils.showToast(SystemInfoActivity.this, getString(R.string.alert_diconnected));
                finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
            }
            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                tvIbeaconRuntime.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dismissLoadingProgressDialog();
                    }
                }, 500);
            }
            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderType orderType = response.orderType;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderType) {
                    case DEVICE_MAC:
                        String hexMac = MokoUtils.bytesToHexString(value);
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
                    case MANUFACTURER:
                        mBeaconDeviceInfo.firmname = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
                        tvIbeaconFirmname.setText(mBeaconDeviceInfo.firmname);
                        break;
                    case SOFT_VERSION:
                        mBeaconDeviceInfo.softVersion = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
                        tvIbeaconSoftVersion.setText(mBeaconDeviceInfo.softVersion);
                        break;
                    case DEVICE_MODEL:
                        mBeaconDeviceInfo.deviceName = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
                        tvIbeaconDeviceName.setText(mBeaconDeviceInfo.deviceName);
                        break;
                    case PRODUCT_DATE:
                        mBeaconDeviceInfo.iBeaconDate = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
                        tvIbeaconDate.setText(mBeaconDeviceInfo.iBeaconDate);
                        break;
                    case HARDWARE_VERSION:
                        mBeaconDeviceInfo.hardwareVersion = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
                        tvIbeaconHardwareVersion.setText(mBeaconDeviceInfo.hardwareVersion);
                        break;
                    case FIRMWARE_VERSION:
                        mBeaconDeviceInfo.firmwareVersion = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
                        tvIbeaconFirmwareVersion.setText(mBeaconDeviceInfo.firmwareVersion);
                        break;
                    case PARAMS_CONFIG:
                        if ("eb59".equals(MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 0, 2)).toLowerCase())) {
                            byte[] runtimeBytes = Arrays.copyOfRange(value, 4, value.length);
                            int seconds = Integer.parseInt(MokoUtils.bytesToHexString(runtimeBytes), 16);
                            int day = 0, hours = 0, minutes = 0;
                            day = seconds / (60 * 60 * 24);
                            seconds -= day * 60 * 60 * 24;
                            hours = seconds / (60 * 60);
                            seconds -= hours * 60 * 60;
                            minutes = seconds / 60;
                            seconds -= minutes * 60;
                            mBeaconDeviceInfo.runtime = String.format("%dD%dh%dm%ds", day, hours, minutes, seconds);
                            tvIbeaconRuntime.setText(mBeaconDeviceInfo.runtime);
                        }
                        if ("eb5b".equals(MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 0, 2)).toLowerCase())) {
                            byte[] chipModelBytes = Arrays.copyOfRange(value, 4, value.length);
                            mBeaconDeviceInfo.chipModel = MokoUtils.hex2String(MokoUtils.bytesToHexString(chipModelBytes));
                            tvIbeaconChipMode.setText(mBeaconDeviceInfo.chipModel);
                        }
                        break;
                }
            }
        });
    }

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

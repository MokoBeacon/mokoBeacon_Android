package com.moko.beacon.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.moko.beacon.BeaconConstants;
import com.moko.beacon.R;
import com.moko.beacon.databinding.ActivitySystemInfoBinding;
import com.moko.beacon.dialog.LoadingDialog;
import com.moko.beacon.entity.BeaconDeviceInfo;
import com.moko.beacon.utils.ToastUtils;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.MokoSupport;
import com.moko.support.OrderTaskAssembler;
import com.moko.support.entity.OrderCHAR;
import com.moko.support.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.Nullable;


public class SystemInfoActivity extends BaseActivity {

    private ActivitySystemInfoBinding mBind;
    private BeaconDeviceInfo mBeaconDeviceInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivitySystemInfoBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
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
            mBind.tvIbeaconSoftVersion.setText(mBeaconDeviceInfo.softVersion);
        }
        if (TextUtils.isEmpty(mBeaconDeviceInfo.firmname)) {
            orderTasks.add(OrderTaskAssembler.getDeviceModel());
        } else {
            mBind.tvIbeaconFirmname.setText(mBeaconDeviceInfo.firmname);
        }
        if (TextUtils.isEmpty(mBeaconDeviceInfo.deviceName)) {
            orderTasks.add(OrderTaskAssembler.getManufacturer());
        } else {
            mBind.tvIbeaconDeviceName.setText(mBeaconDeviceInfo.deviceName);
        }
        if (TextUtils.isEmpty(mBeaconDeviceInfo.iBeaconDate)) {
            orderTasks.add(OrderTaskAssembler.getProductDate());
        } else {
            mBind.tvIbeaconDate.setText(mBeaconDeviceInfo.iBeaconDate);
        }
        if (TextUtils.isEmpty(mBeaconDeviceInfo.iBeaconMac)) {
            orderTasks.add(OrderTaskAssembler.getDeviceMac());
        } else {
            mBind.tvIbeaconMac.setText(mBeaconDeviceInfo.iBeaconMac);
        }
        if (TextUtils.isEmpty(mBeaconDeviceInfo.chipModel)) {
            orderTasks.add(OrderTaskAssembler.getChipModel());
        } else {
            mBind.tvIbeaconChipMode.setText(mBeaconDeviceInfo.chipModel);
        }
        if (TextUtils.isEmpty(mBeaconDeviceInfo.hardwareVersion)) {
            orderTasks.add(OrderTaskAssembler.getHardwareVersion());
        } else {
            mBind.tvIbeaconHardwareVersion.setText(mBeaconDeviceInfo.hardwareVersion);
        }
        if (TextUtils.isEmpty(mBeaconDeviceInfo.firmwareVersion)) {
            orderTasks.add(OrderTaskAssembler.getFirmwareVersion());
        } else {
            mBind.tvIbeaconFirmwareVersion.setText(mBeaconDeviceInfo.firmwareVersion);
        }
        if (TextUtils.isEmpty(mBeaconDeviceInfo.runtime)) {
            orderTasks.add(OrderTaskAssembler.getRuntime());
        } else {
            mBind.tvIbeaconRuntime.setText(mBeaconDeviceInfo.runtime);
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
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }


    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
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
                mBind.tvIbeaconRuntime.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dismissLoadingProgressDialog();
                    }
                }, 500);
            }
            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderCHAR) {
                    case CHAR_DEVICE_MAC:
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
                            mBind.tvIbeaconMac.setText(mBeaconDeviceInfo.iBeaconMac);
                        }
                        break;
                    case CHAR_MANUFACTURER_NAME:
                        mBeaconDeviceInfo.firmname = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
                        mBind.tvIbeaconFirmname.setText(mBeaconDeviceInfo.firmname);
                        break;
                    case CHAR_SOFTWARE_REVISION:
                        mBeaconDeviceInfo.softVersion = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
                        mBind.tvIbeaconSoftVersion.setText(mBeaconDeviceInfo.softVersion);
                        break;
                    case CHAR_MODEL_NUMBER:
                        mBeaconDeviceInfo.deviceName = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
                        mBind.tvIbeaconDeviceName.setText(mBeaconDeviceInfo.deviceName);
                        break;
                    case CHAR_SERIAL_NUMBER:
                        mBeaconDeviceInfo.iBeaconDate = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
                        mBind.tvIbeaconDate.setText(mBeaconDeviceInfo.iBeaconDate);
                        break;
                    case CHAR_HARDWARE_REVISION:
                        mBeaconDeviceInfo.hardwareVersion = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
                        mBind.tvIbeaconHardwareVersion.setText(mBeaconDeviceInfo.hardwareVersion);
                        break;
                    case CHAR_FIRMWARE_REVISION:
                        mBeaconDeviceInfo.firmwareVersion = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
                        mBind.tvIbeaconFirmwareVersion.setText(mBeaconDeviceInfo.firmwareVersion);
                        break;
                    case CHAR_PARAMS:
                        if (value.length > 4) {
                            int header = value[0] & 0xFF;// 0xED
                            int cmd = value[1] & 0xFF;
                            if (header != 0xEB)
                                return;
                            int length = MokoUtils.toInt(Arrays.copyOfRange(value, 2, 4));
                            ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                            if (configKeyEnum == null) {
                                return;
                            }
                            switch (configKeyEnum) {
                                case GET_CHIP_MODEL:
                                    byte[] chipModelBytes = Arrays.copyOfRange(value, 4, value.length);
                                    mBeaconDeviceInfo.chipModel = MokoUtils.hex2String(MokoUtils.bytesToHexString(chipModelBytes));
                                    mBind.tvIbeaconChipMode.setText(mBeaconDeviceInfo.chipModel);
                                    break;
                                case GET_RUNTIME:
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
                                    mBind.tvIbeaconRuntime.setText(mBeaconDeviceInfo.runtime);
                                    break;
                            }
                        }
                        break;
                }
            }
        });
    }

    public void onBack(View view) {
        if (isWindowLocked()) return;
        back();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    private void back() {
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        Intent intent = new Intent();
        intent.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_INFO, mBeaconDeviceInfo);
        setResult(RESULT_OK);
        finish();
    }

    private LoadingDialog mLoadingDialog;

    private void showLoadingProgressDialog() {
        mLoadingDialog = new LoadingDialog();
        mLoadingDialog.show(getSupportFragmentManager());

    }

    private void dismissLoadingProgressDialog() {
        if (mLoadingDialog != null)
            mLoadingDialog.dismissAllowingStateLoss();
    }
}

package com.moko.beacon.activity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.moko.beacon.BeaconConstants;
import com.moko.beacon.R;
import com.moko.beacon.dialog.BeaconAlertDialog;
import com.moko.beacon.entity.BeaconParam;
import com.moko.beacon.service.DfuService;
import com.moko.beacon.utils.FileUtils;
import com.moko.beacon.utils.ToastUtils;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.OrderTaskAssembler;
import com.moko.support.entity.OrderType;
import com.moko.support.event.ConnectStatusEvent;
import com.moko.support.event.OrderTaskResponseEvent;
import com.moko.support.log.LogModule;
import com.moko.support.task.OrderTask;
import com.moko.support.task.OrderTaskResponse;
import com.moko.support.utils.MokoUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import no.nordicsemi.android.dfu.DfuLogListener;
import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;

/**
 * @Date 2017/12/13 0013
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beacon.activity.DeviceInfoActivity
 */
public class DeviceInfoActivity extends BaseActivity {

    public static final int REQUEST_CODE_SELECT_FIRMWARE = 0x10;

    @BindView(R.id.tv_conn_state)
    TextView tvConnState;
    @BindView(R.id.tv_ibeacon_battery)
    TextView tvIbeaconBattery;
    @BindView(R.id.tv_ibeacon_uuid)
    TextView tvIbeaconUuid;
    @BindView(R.id.tv_ibeacon_major)
    TextView tvIbeaconMajor;
    @BindView(R.id.tv_ibeacon_minor)
    TextView tvIbeaconMinor;
    @BindView(R.id.tv_ibeacon_measure_power)
    TextView tvIbeaconMeasurePower;
    @BindView(R.id.tv_ibeacon_transmission)
    TextView tvIbeaconTransmission;
    @BindView(R.id.tv_ibeacon_broadcasting_interval)
    TextView tvIbeaconBroadcastingInterval;
    @BindView(R.id.tv_ibeacon_serialID)
    TextView tvIbeaconSerialID;
    @BindView(R.id.tv_ibeacon_mac)
    TextView tvIbeaconMac;
    @BindView(R.id.tv_ibeacon_device_name)
    TextView tvIbeaconDeviceName;
    @BindView(R.id.iv_ibeacon_device_conn_mode)
    ImageView ivIbeaconDeviceConnMode;
    @BindView(R.id.rl_ibeacon_three_axis)
    RelativeLayout rlIbeaconThreeAxis;
    @BindView(R.id.view_cover)
    View viewCover;
    private BeaconParam mBeaconParam;

    private boolean mIsCloseConnectable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);
        ButterKnife.bind(this);
        mBeaconParam = (BeaconParam) getIntent().getSerializableExtra(BeaconConstants.EXTRA_KEY_DEVICE_PARAM);
        if (mBeaconParam == null) {
            finish();
            return;
        }
        rlIbeaconThreeAxis.setVisibility(!TextUtils.isEmpty(mBeaconParam.threeAxis) ? View.VISIBLE : View.GONE);
        if (MokoSupport.getInstance().isConnDevice(this, mBeaconParam.iBeaconMAC)) {
            tvConnState.setText(getString(R.string.device_info_conn_status_disconnect));
            viewCover.setVisibility(View.GONE);
        } else {
            tvConnState.setText(getString(R.string.device_info_conn_status_connect));
            viewCover.setVisibility(View.VISIBLE);
        }
        changeValue();

        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
    }

    private void changeValue() {
        tvIbeaconBattery.setText(mBeaconParam.battery);
        tvIbeaconUuid.setText(mBeaconParam.uuid);
        tvIbeaconMajor.setText(mBeaconParam.major);
        tvIbeaconMinor.setText(mBeaconParam.minor);
        tvIbeaconMeasurePower.setText(String.format("-%sdBm", mBeaconParam.measurePower));
        tvIbeaconTransmission.setText(mBeaconParam.transmission);
        tvIbeaconBroadcastingInterval.setText(mBeaconParam.broadcastingInterval);
        tvIbeaconSerialID.setText(mBeaconParam.serialID);
        tvIbeaconMac.setText(mBeaconParam.iBeaconMAC);
        tvIbeaconDeviceName.setText(mBeaconParam.iBeaconName);
        boolean isConnectable = "00".equals(mBeaconParam.connectionMode);
        ivIbeaconDeviceConnMode.setImageDrawable(ContextCompat.getDrawable(this, isConnectable ? R.drawable.connectable_checked : R.drawable.connectable_unchecked));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 100)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_CONN_STATUS_DISCONNECTED.equals(action)) {
                tvConnState.setText(getString(R.string.device_info_conn_status_connect));
                viewCover.setVisibility(View.VISIBLE);
                ToastUtils.showToast(DeviceInfoActivity.this, "Connect Failed");
                dismissLoadingProgressDialog();
                dismissSyncProgressDialog();
            }
            if (MokoConstants.ACTION_DISCOVER_SUCCESS.equals(action)) {
                tvConnState.setText(getString(R.string.device_info_conn_status_disconnect));
                viewCover.setVisibility(View.GONE);
                // 读取全部可读数据
                tvConnState.postDelayed(() -> {
                    // open password notify and set passwrord
                    List<OrderTask> orderTasks = new ArrayList<>();
                    orderTasks.add(OrderTaskAssembler.getBattery());
                    orderTasks.add(OrderTaskAssembler.getSoftVersion());
                    orderTasks.add(OrderTaskAssembler.getDeviceModel());
                    orderTasks.add(OrderTaskAssembler.getManufacturer());
                    orderTasks.add(OrderTaskAssembler.getProductDate());
                    orderTasks.add(OrderTaskAssembler.getHardwareVersion());
                    orderTasks.add(OrderTaskAssembler.getFirmwareVersion());
                    orderTasks.add(OrderTaskAssembler.getSoftVersion());
                    orderTasks.add(OrderTaskAssembler.getDeviceUUID());
                    orderTasks.add(OrderTaskAssembler.getMajor());
                    orderTasks.add(OrderTaskAssembler.getMinor());
                    orderTasks.add(OrderTaskAssembler.getMeasurePower());
                    orderTasks.add(OrderTaskAssembler.getTransmission());
                    orderTasks.add(OrderTaskAssembler.getAdvInterval());
                    orderTasks.add(OrderTaskAssembler.getSerialID());
                    orderTasks.add(OrderTaskAssembler.getAdvName());
                    orderTasks.add(OrderTaskAssembler.getConnection());
                    orderTasks.add(OrderTaskAssembler.getDeviceMac());
                    orderTasks.add(OrderTaskAssembler.getRunntime());
                    orderTasks.add(OrderTaskAssembler.getChipModel());
                    orderTasks.add(OrderTaskAssembler.setOvertime());
                    orderTasks.add(OrderTaskAssembler.setPassword(mBeaconParam.password));
                    MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));

                }, 500);
            }
        });

    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 100)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderType orderType = response.orderType;
                int responseType = response.responseType;
                byte[] value = response.responseValue;

            }
            if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
            }
            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                tvConnState.postDelayed(() -> {
                    dismissLoadingProgressDialog();
                    dismissSyncProgressDialog();
                }, 500);
            }
            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderType orderType = response.orderType;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderType) {
                    case BATTERY:
                        mBeaconParam.battery = Integer.parseInt(MokoUtils.bytesToHexString(value), 16) + "";
                        tvIbeaconBattery.setText(mBeaconParam.battery);
                        break;
                    case DEVICE_UUID:
                        // 读取UUID成功
                        // ToastUtils.showToast(DeviceInfoActivity.this, "读取UUID成功");
                        String hexString = MokoUtils.bytesToHexString(value).toUpperCase();
                        if (hexString.length() > 31) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(hexString.substring(0, 8));
                            sb.append("-");
                            sb.append(hexString.substring(8, 12));
                            sb.append("-");
                            sb.append(hexString.substring(12, 16));
                            sb.append("-");
                            sb.append(hexString.substring(16, 20));
                            sb.append("-");
                            sb.append(hexString.substring(20, 32));
                            String uuid = sb.toString();
                            mBeaconParam.uuid = uuid;
                            tvIbeaconUuid.setText(uuid);
                            tvIbeaconUuid.setText(mBeaconParam.uuid);
                        }
                        break;
                    case MAJOR:
                        mBeaconParam.major = Integer.parseInt(MokoUtils.bytesToHexString(value), 16) + "";
                        tvIbeaconMajor.setText(mBeaconParam.major);
                        break;
                    case MINOR:
                        mBeaconParam.minor = Integer.parseInt(MokoUtils.bytesToHexString(value), 16) + "";
                        tvIbeaconMinor.setText(mBeaconParam.minor);
                        break;
                    case MEASURE_POWER:
                        mBeaconParam.measurePower = Integer.parseInt(MokoUtils.bytesToHexString(value), 16) + "";
                        tvIbeaconMeasurePower.setText(String.format("-%sdBm", mBeaconParam.measurePower));
                        break;
                    case TRANSMISSION:
                        int transmission = Integer.parseInt(MokoUtils.bytesToHexString(value), 16);
                        if (transmission == 8) {
                            transmission = 7;
                        }
                        mBeaconParam.transmission = transmission + "";
                        tvIbeaconTransmission.setText(mBeaconParam.transmission);
                        break;
                    case ADV_INTERVAL:
                        mBeaconParam.broadcastingInterval = Integer.parseInt(MokoUtils.bytesToHexString(value), 16) + "";
                        tvIbeaconBroadcastingInterval.setText(mBeaconParam.broadcastingInterval);
                        break;
                    case SERIAL_ID:
                        mBeaconParam.serialID = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
                        tvIbeaconSerialID.setText(mBeaconParam.serialID);
                        break;
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
                            mBeaconParam.iBeaconMAC = mac;
                            mBeaconParam.beaconInfo.iBeaconMac = mac;
                            tvIbeaconMac.setText(mBeaconParam.iBeaconMAC);
                        }
                        break;
                    case ADV_NAME:
                        mBeaconParam.iBeaconName = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
                        tvIbeaconDeviceName.setText(mBeaconParam.iBeaconName);
                        break;
                    case CONNECTION:
                        if (responseType == OrderTask.RESPONSE_TYPE_READ) {
                            mBeaconParam.connectionMode = MokoUtils.bytesToHexString(value);
                            boolean isConnectable = "00".equals(mBeaconParam.connectionMode);
                            ivIbeaconDeviceConnMode.setImageDrawable(ContextCompat.getDrawable(DeviceInfoActivity.this, isConnectable ? R.drawable.connectable_checked : R.drawable.connectable_unchecked));
                            if (mIsCloseConnectable && !isConnectable) {
                                mIsCloseConnectable = false;
                                dismissSyncProgressDialog();
                                back();
                            }
                        } else {
                            dismissSyncProgressDialog();
                            mIsCloseConnectable = true;
                            mBeaconParam.connectionMode = null;
                            getEmptyInfo();
                        }
                        break;
                    case MANUFACTURER:
                        mBeaconParam.beaconInfo.firmname = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
                        break;
                    case SOFT_VERSION:
                        mBeaconParam.beaconInfo.softVersion = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
                        break;
                    case DEVICE_MODEL:
                        mBeaconParam.beaconInfo.deviceName = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
                        break;
                    case PRODUCT_DATE:
                        mBeaconParam.beaconInfo.iBeaconDate = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
                        break;
                    case HARDWARE_VERSION:
                        mBeaconParam.beaconInfo.hardwareVersion = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
                        break;
                    case FIRMWARE_VERSION:
                        mBeaconParam.beaconInfo.firmwareVersion = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
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
                            mBeaconParam.beaconInfo.runtime = String.format("%dD%dh%dm%ds", day, hours, minutes, seconds);
                        }
                        if ("eb5b".equals(MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 0, 2)).toLowerCase())) {
                            byte[] chipModelBytes = Arrays.copyOfRange(value, 4, value.length);
                            mBeaconParam.beaconInfo.chipModel = MokoUtils.hex2String(MokoUtils.bytesToHexString(chipModelBytes));
                        }
                        if ("eb6d".equals(MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 0, 2)).toLowerCase())) {
                            if ((value[value.length - 1] & 0xff) == 0xAA) {
                                dismissSyncProgressDialog();
                                ToastUtils.showToast(DeviceInfoActivity.this, "Power off successfully");
                                back();
                            } else {
                                ToastUtils.showToast(DeviceInfoActivity.this, "Power off failed");
                            }
                        }
                        break;
                    case PASSWORD:
                        if ("00".equals(MokoUtils.bytesToHexString(value))) {
                            changeValue();
                        }
                        break;
                }
            }
        });
    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                        case BluetoothAdapter.STATE_OFF:
                            tvConnState.setText(getString(R.string.device_info_conn_status_connect));
                            viewCover.setVisibility(View.VISIBLE);
                            ToastUtils.showToast(DeviceInfoActivity.this, "Connect Failed");
                            dismissLoadingProgressDialog();
                            dismissSyncProgressDialog();
                            dismissDFUProgressDialog();
                            final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(DeviceInfoActivity.this);
                            final Intent abortAction = new Intent(DfuService.BROADCAST_ACTION);
                            abortAction.putExtra(DfuService.EXTRA_ACTION, DfuService.ACTION_ABORT);
                            manager.sendBroadcast(abortAction);
                            break;
                    }
                }
            }
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
        if (MokoSupport.getInstance().isConnDevice(this, mBeaconParam.iBeaconMAC)) {
            MokoSupport.getInstance().disConnectBle();
        }
        finish();
    }


    @OnClick({R.id.tv_back, R.id.tv_conn_state, R.id.rl_ibeacon_battery, R.id.rl_ibeacon_uuid,
            R.id.rl_ibeacon_major, R.id.rl_ibeacon_minor, R.id.rl_ibeacon_measure_power,
            R.id.rl_ibeacon_transmission, R.id.rl_ibeacon_broadcasting_interval, R.id.rl_ibeacon_serialID,
            R.id.rl_ibeacon_mac, R.id.rl_ibeacon_device_name, R.id.iv_ibeacon_device_conn_mode,
            R.id.rl_ibeacon_change_password, R.id.rl_ibeacon_device_info, R.id.rl_ibeacon_three_axis,
            R.id.rl_ibeacon_dfu, R.id.view_cover, R.id.iv_ibeacon_device_power})
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.view_cover:
                ToastUtils.showToast(this, R.string.disconnect_alert);
                break;
            case R.id.tv_back:
                back();
                break;
            case R.id.tv_conn_state:
                if (!MokoSupport.getInstance().isBluetoothOpen()) {
                    ToastUtils.showToast(this, "bluetooth is closed,please open");
                    return;
                }
                if (!MokoSupport.getInstance().isConnDevice(this, mBeaconParam.iBeaconMAC)) {
                    MokoSupport.getInstance().connDevice(DeviceInfoActivity.this, mBeaconParam.iBeaconMAC);
                    showLoadingProgressDialog(getString(R.string.dialog_connecting));
                } else {
                    tvConnState.setText(getString(R.string.device_info_conn_status_connect));
                    MokoSupport.getInstance().disConnectBle();
                    viewCover.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.rl_ibeacon_uuid:
                if (!MokoSupport.getInstance().isBluetoothOpen()) {
                    ToastUtils.showToast(this, "bluetooth is closed,please open");
                    return;
                }
                if (!MokoSupport.getInstance().isConnDevice(this, mBeaconParam.iBeaconMAC)) {
                    ToastUtils.showToast(this, getString(R.string.alert_click_reconnect));
                    return;
                }
                intent = new Intent(this, SetUUIDActivity.class);
                intent.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_UUID, mBeaconParam.uuid);
                startActivityForResult(intent, BeaconConstants.REQUEST_CODE_SET_UUID);
                break;
            case R.id.rl_ibeacon_major:
                if (!MokoSupport.getInstance().isBluetoothOpen()) {
                    ToastUtils.showToast(this, "bluetooth is closed,please open");
                    return;
                }
                if (!MokoSupport.getInstance().isConnDevice(this, mBeaconParam.iBeaconMAC)) {
                    ToastUtils.showToast(this, getString(R.string.alert_click_reconnect));
                    return;
                }
                intent = new Intent(this, SetMajorActivity.class);
                intent.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_MAJOR, Integer.parseInt(mBeaconParam.major));
                startActivityForResult(intent, BeaconConstants.REQUEST_CODE_SET_MAJOR);
                break;
            case R.id.rl_ibeacon_minor:
                if (!MokoSupport.getInstance().isBluetoothOpen()) {
                    ToastUtils.showToast(this, "bluetooth is closed,please open");
                    return;
                }
                if (!MokoSupport.getInstance().isConnDevice(this, mBeaconParam.iBeaconMAC)) {
                    ToastUtils.showToast(this, getString(R.string.alert_click_reconnect));
                    return;
                }
                intent = new Intent(this, SetMinorActivity.class);
                intent.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_MINOR, Integer.parseInt(mBeaconParam.minor));
                startActivityForResult(intent, BeaconConstants.REQUEST_CODE_SET_MINOR);
                break;
            case R.id.rl_ibeacon_measure_power:
                if (!MokoSupport.getInstance().isBluetoothOpen()) {
                    ToastUtils.showToast(this, "bluetooth is closed,please open");
                    return;
                }
                if (!MokoSupport.getInstance().isConnDevice(this, mBeaconParam.iBeaconMAC)) {
                    ToastUtils.showToast(this, getString(R.string.alert_click_reconnect));
                    return;
                }
                intent = new Intent(this, SetMeasurePowerActivity.class);
                intent.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_MEASURE_POWER, Integer.parseInt(mBeaconParam.measurePower));
                startActivityForResult(intent, BeaconConstants.REQUEST_CODE_SET_MEASURE_POWER);
                break;
            case R.id.rl_ibeacon_transmission:
                if (!MokoSupport.getInstance().isBluetoothOpen()) {
                    ToastUtils.showToast(this, "bluetooth is closed,please open");
                    return;
                }
                if (!MokoSupport.getInstance().isConnDevice(this, mBeaconParam.iBeaconMAC)) {
                    ToastUtils.showToast(this, getString(R.string.alert_click_reconnect));
                    return;
                }
                intent = new Intent(this, SetTransmissionActivity.class);
                intent.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_TRANSMISSION, Integer.parseInt(mBeaconParam.transmission));
                startActivityForResult(intent, BeaconConstants.REQUEST_CODE_SET_TRANSMISSION);
                break;
            case R.id.rl_ibeacon_broadcasting_interval:
                if (!MokoSupport.getInstance().isBluetoothOpen()) {
                    ToastUtils.showToast(this, "bluetooth is closed,please open");
                    return;
                }
                if (!MokoSupport.getInstance().isConnDevice(this, mBeaconParam.iBeaconMAC)) {
                    ToastUtils.showToast(this, getString(R.string.alert_click_reconnect));
                    return;
                }
                intent = new Intent(this, SetBroadcastIntervalActivity.class);
                intent.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_BROADCASTINTERVAL, Integer.parseInt(mBeaconParam.broadcastingInterval));
                startActivityForResult(intent, BeaconConstants.REQUEST_CODE_SET_BROADCASTINTERVAL);
                break;
            case R.id.rl_ibeacon_serialID:
                if (!MokoSupport.getInstance().isBluetoothOpen()) {
                    ToastUtils.showToast(this, "bluetooth is closed,please open");
                    return;
                }
                if (!MokoSupport.getInstance().isConnDevice(this, mBeaconParam.iBeaconMAC)) {
                    ToastUtils.showToast(this, getString(R.string.alert_click_reconnect));
                    return;
                }
                intent = new Intent(this, SetDeviceIdActivity.class);
                intent.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_DEVICE_ID, mBeaconParam.serialID);
                startActivityForResult(intent, BeaconConstants.REQUEST_CODE_SET_DEVICE_ID);
                break;
            case R.id.rl_ibeacon_device_name:
                if (!MokoSupport.getInstance().isBluetoothOpen()) {
                    ToastUtils.showToast(this, "bluetooth is closed,please open");
                    return;
                }
                if (!MokoSupport.getInstance().isConnDevice(this, mBeaconParam.iBeaconMAC)) {
                    ToastUtils.showToast(this, getString(R.string.alert_click_reconnect));
                    return;
                }
                intent = new Intent(this, SetIBeaconNameActivity.class);
                intent.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_IBEACON_NAME, mBeaconParam.iBeaconName);
                intent.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_IBEACON_THREE_AXIS, mBeaconParam.threeAxis);
                startActivityForResult(intent, BeaconConstants.REQUEST_CODE_SET_IBEACON_NAME);
                break;
            case R.id.iv_ibeacon_device_conn_mode:
                if (!MokoSupport.getInstance().isBluetoothOpen()) {
                    ToastUtils.showToast(this, "bluetooth is closed,please open");
                    return;
                }
                if (!MokoSupport.getInstance().isConnDevice(this, mBeaconParam.iBeaconMAC)) {
                    ToastUtils.showToast(this, getString(R.string.alert_click_reconnect));
                    return;
                }
                final boolean isConnectable = "00".equals(mBeaconParam.connectionMode);
                final BeaconAlertDialog connectAlertDialog = new BeaconAlertDialog(this);
                connectAlertDialog.setData(isConnectable ? "Are you sure to make device disconnectable?" : "Are you sure to make device connectable?");
                connectAlertDialog.setConnectAlertClickListener(new BeaconAlertDialog.ConnectAlertClickListener() {
                    @Override
                    public void onEnsureClicked() {
                        showSyncProgressDialog("Syncing...");
                        String connectMode = !isConnectable ? "00" : "01";
                        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setConnection(connectMode));
                    }

                    @Override
                    public void onDismiss() {

                    }
                });
                connectAlertDialog.show();
                break;
            case R.id.rl_ibeacon_change_password:
                if (!MokoSupport.getInstance().isBluetoothOpen()) {
                    ToastUtils.showToast(this, "bluetooth is closed,please open");
                    return;
                }
                if (!MokoSupport.getInstance().isConnDevice(this, mBeaconParam.iBeaconMAC)) {
                    ToastUtils.showToast(this, getString(R.string.alert_click_reconnect));
                    return;
                }
                intent = new Intent(this, SetPasswordActivity.class);
                startActivityForResult(intent, BeaconConstants.REQUEST_CODE_SET_PASSWORD);
                break;
            case R.id.rl_ibeacon_device_info:
                if (!MokoSupport.getInstance().isBluetoothOpen()) {
                    ToastUtils.showToast(this, "bluetooth is closed,please open");
                    return;
                }
                if (!MokoSupport.getInstance().isConnDevice(this, mBeaconParam.iBeaconMAC)) {
                    ToastUtils.showToast(this, getString(R.string.alert_click_reconnect));
                    return;
                }
                intent = new Intent(this, SystemInfoActivity.class);
                intent.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_INFO, mBeaconParam.beaconInfo);
                startActivityForResult(intent, BeaconConstants.REQUEST_CODE_SET_SYSTEM_INFO);
                break;
            case R.id.rl_ibeacon_three_axis:
                if (!MokoSupport.getInstance().isBluetoothOpen()) {
                    ToastUtils.showToast(this, "bluetooth is closed,please open");
                    return;
                }
                if (!MokoSupport.getInstance().isConnDevice(this, mBeaconParam.iBeaconMAC)) {
                    ToastUtils.showToast(this, getString(R.string.alert_click_reconnect));
                    return;
                }
                intent = new Intent(this, ThreeAxesActivity.class);
                startActivityForResult(intent, BeaconConstants.REQUEST_CODE_SET_THREE_AXIS);
                break;
            case R.id.rl_ibeacon_mac:
                ToastUtils.showToast(this, getString(R.string.device_info_cannot_modify));
                break;
            case R.id.rl_ibeacon_battery:
                ToastUtils.showToast(this, getString(R.string.device_info_cannot_modify));
                break;
            case R.id.iv_ibeacon_device_power:
                final BeaconAlertDialog powerAlertDialog = new BeaconAlertDialog(this);
                powerAlertDialog.setData("Are you sure to turn off the device?Please make sure the device has a button to turn on!");
                powerAlertDialog.setConnectAlertClickListener(new BeaconAlertDialog.ConnectAlertClickListener() {
                    @Override
                    public void onEnsureClicked() {
                        showSyncProgressDialog("Syncing...");
                        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setClose());
                    }

                    @Override
                    public void onDismiss() {

                    }
                });
                powerAlertDialog.show();
                break;
            case R.id.rl_ibeacon_dfu:
                if (!MokoSupport.getInstance().isBluetoothOpen()) {
                    ToastUtils.showToast(this, "bluetooth is closed,please open");
                    return;
                }
                if (!MokoSupport.getInstance().isConnDevice(this, mBeaconParam.iBeaconMAC)) {
                    ToastUtils.showToast(this, getString(R.string.alert_click_reconnect));
                    return;
                }
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                try {
                    startActivityForResult(Intent.createChooser(intent, "select file first!"), REQUEST_CODE_SELECT_FIRMWARE);
                } catch (ActivityNotFoundException ex) {
                    ToastUtils.showToast(this, "install file manager app");
                }
                break;
        }
    }

    private ProgressDialog mLoadingDialog;

    private void showLoadingProgressDialog(String tips) {
        mLoadingDialog = new ProgressDialog(DeviceInfoActivity.this);
        mLoadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mLoadingDialog.setCanceledOnTouchOutside(false);
        mLoadingDialog.setCancelable(false);
        mLoadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mLoadingDialog.setMessage(tips);
        if (!isFinishing() && mLoadingDialog != null && !mLoadingDialog.isShowing()) {
            mLoadingDialog.show();
        }
    }

    private void dismissLoadingProgressDialog() {
        if (!isFinishing() && mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    private ProgressDialog mSyncingDialog;

    private void showSyncProgressDialog(String tips) {
        mSyncingDialog = new ProgressDialog(DeviceInfoActivity.this);
        mSyncingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mSyncingDialog.setCanceledOnTouchOutside(false);
        mSyncingDialog.setCancelable(false);
        mSyncingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mSyncingDialog.setMessage(tips);
        if (!isFinishing() && mSyncingDialog != null && !mSyncingDialog.isShowing()) {
            mSyncingDialog.show();
        }
    }

    private void dismissSyncProgressDialog() {
        if (!isFinishing() && mSyncingDialog != null && mSyncingDialog.isShowing()) {
            mSyncingDialog.dismiss();
        }
    }

    private ProgressDialog mDFUDialog;

    private void showDFUProgressDialog(String tips) {
        mDFUDialog = new ProgressDialog(DeviceInfoActivity.this);
        mDFUDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDFUDialog.setCanceledOnTouchOutside(false);
        mDFUDialog.setCancelable(false);
        mDFUDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDFUDialog.setMessage(tips);
        if (!isFinishing() && mDFUDialog != null && !mDFUDialog.isShowing()) {
            mDFUDialog.show();
        }
    }

    private void dismissDFUProgressDialog() {
        mDeviceConnectCount = 0;
        if (!isFinishing() && mDFUDialog != null && mDFUDialog.isShowing()) {
            mDFUDialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == BeaconConstants.RESULT_CONN_DISCONNECTED) {
            tvConnState.setText(getString(R.string.device_info_conn_status_connect));
            viewCover.setVisibility(View.VISIBLE);
        } else {
            switch (requestCode) {
                case BeaconConstants.REQUEST_CODE_SET_UUID:
                    if (resultCode == RESULT_OK) {
                        mBeaconParam.uuid = null;
                    }
                    break;
                case BeaconConstants.REQUEST_CODE_SET_MAJOR:
                    if (resultCode == RESULT_OK) {
                        mBeaconParam.major = null;
                    }
                    break;
                case BeaconConstants.REQUEST_CODE_SET_MINOR:
                    if (resultCode == RESULT_OK) {
                        mBeaconParam.minor = null;
                    }
                    break;
                case BeaconConstants.REQUEST_CODE_SET_MEASURE_POWER:
                    if (resultCode == RESULT_OK) {
                        mBeaconParam.measurePower = null;
                    }
                    break;
                case BeaconConstants.REQUEST_CODE_SET_TRANSMISSION:
                    if (resultCode == RESULT_OK) {
                        mBeaconParam.transmission = null;
                    }
                    break;
                case BeaconConstants.REQUEST_CODE_SET_BROADCASTINTERVAL:
                    if (resultCode == RESULT_OK) {
                        mBeaconParam.broadcastingInterval = null;
                    }
                    break;
                case BeaconConstants.REQUEST_CODE_SET_DEVICE_ID:
                    if (resultCode == RESULT_OK) {
                        mBeaconParam.serialID = null;
                    }
                    break;
                case BeaconConstants.REQUEST_CODE_SET_IBEACON_NAME:
                    if (resultCode == RESULT_OK) {
                        mBeaconParam.iBeaconName = null;
                    }
                    break;
                case BeaconConstants.REQUEST_CODE_SET_CONNECTION_MODE:
                    if (resultCode == RESULT_OK) {
                        mBeaconParam.connectionMode = null;
                    }
                    break;
                case BeaconConstants.REQUEST_CODE_SET_PASSWORD:
                    if (resultCode == RESULT_OK) {
                        if (data != null && data.getExtras() != null) {
                            String password = data.getExtras().getString(BeaconConstants.EXTRA_KEY_DEVICE_PASSWORD);
                            mBeaconParam.password = password;
                            back();
                        }
                        return;
                    }
                case REQUEST_CODE_SELECT_FIRMWARE:
                    if (resultCode == RESULT_OK) {
                        if (!MokoSupport.getInstance().isConnDevice(this, mBeaconParam.iBeaconMAC)) {
                            ToastUtils.showToast(this, getString(R.string.alert_click_reconnect));
                            return;
                        }
                        //得到uri，后面就是将uri转化成file的过程。
                        Uri uri = data.getData();
                        String firmwareFilePath = FileUtils.getPath(this, uri);
                        if (TextUtils.isEmpty(firmwareFilePath)) {
                            ToastUtils.showToast(this, "file is not exists!");
                            return;
                        }
                        final File firmwareFile = new File(firmwareFilePath);
                        if (firmwareFile.exists()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                DfuServiceInitiator.createDfuNotificationChannel(this);
                            }
                            final DfuServiceInitiator starter = new DfuServiceInitiator(mBeaconParam.iBeaconMAC)
                                    .setDeviceName(mBeaconParam.iBeaconName)
                                    .setKeepBond(false)
                                    .setPacketsReceiptNotificationsEnabled(true)
                                    .setDisableNotification(true);
                            starter.setZip(null, firmwareFilePath);
                            starter.start(this, DfuService.class);
                            showDFUProgressDialog("Waiting...");
                        } else {
                            ToastUtils.showToast(this, "file is not exists!");
                        }
                    }
                    break;
            }
            getEmptyInfo();
        }

    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private void getEmptyInfo() {
        final ArrayList<OrderTask> orderTasks = new ArrayList<>();

        if (TextUtils.isEmpty(mBeaconParam.battery)) {
            orderTasks.add(OrderTaskAssembler.getBattery());
        }
        if (TextUtils.isEmpty(mBeaconParam.uuid)) {
            orderTasks.add(OrderTaskAssembler.getDeviceUUID());
        }
        if (TextUtils.isEmpty(mBeaconParam.major)) {
            orderTasks.add(OrderTaskAssembler.getMajor());
        }
        if (TextUtils.isEmpty(mBeaconParam.minor)) {
            orderTasks.add(OrderTaskAssembler.getMinor());
        }
        if (TextUtils.isEmpty(mBeaconParam.measurePower)) {
            orderTasks.add(OrderTaskAssembler.getMeasurePower());
        }
        if (TextUtils.isEmpty(mBeaconParam.transmission)) {
            orderTasks.add(OrderTaskAssembler.getTransmission());
        }
        if (TextUtils.isEmpty(mBeaconParam.broadcastingInterval)) {
            orderTasks.add(OrderTaskAssembler.getAdvInterval());
        }
        if (TextUtils.isEmpty(mBeaconParam.serialID)) {
            orderTasks.add(OrderTaskAssembler.getSerialID());
        }
        if (TextUtils.isEmpty(mBeaconParam.iBeaconName)) {
            orderTasks.add(OrderTaskAssembler.getAdvName());
        }
        if (TextUtils.isEmpty(mBeaconParam.iBeaconMAC)) {
            orderTasks.add(OrderTaskAssembler.getDeviceMac());
        }
        if (TextUtils.isEmpty(mBeaconParam.connectionMode)) {
            orderTasks.add(OrderTaskAssembler.getConnection());
        }
        if (!orderTasks.isEmpty()) {
            if (!MokoSupport.getInstance().isBluetoothOpen()) {
                ToastUtils.showToast(this, "bluetooth is closed,please open");
                return;
            }
            showSyncProgressDialog("Syncing...");
            tvConnState.postDelayed(new Runnable() {
                @Override
                public void run() {
                    orderTasks.add(OrderTaskAssembler.setOvertime());
                    MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
                }
            }, 500);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        DfuServiceListenerHelper.registerProgressListener(this, mDfuProgressListener);
        DfuServiceListenerHelper.registerLogListener(this, mDfuLogListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DfuServiceListenerHelper.unregisterProgressListener(this, mDfuProgressListener);
        DfuServiceListenerHelper.unregisterLogListener(this, mDfuLogListener);
    }

    private int mDeviceConnectCount;

    private final DfuProgressListener mDfuProgressListener = new DfuProgressListenerAdapter() {
        @Override
        public void onDeviceConnecting(String deviceAddress) {
            LogModule.w("onDeviceConnecting...");
            mDeviceConnectCount++;
            if (mDeviceConnectCount > 3) {
                Toast.makeText(DeviceInfoActivity.this, "Error:DFU Failed", Toast.LENGTH_SHORT).show();
                dismissDFUProgressDialog();
                final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(DeviceInfoActivity.this);
                final Intent abortAction = new Intent(DfuService.BROADCAST_ACTION);
                abortAction.putExtra(DfuService.EXTRA_ACTION, DfuService.ACTION_ABORT);
                manager.sendBroadcast(abortAction);
            }
        }

        @Override
        public void onDeviceDisconnecting(String deviceAddress) {
            LogModule.w("onDeviceDisconnecting...");
        }

        @Override
        public void onDfuProcessStarting(String deviceAddress) {
            mDFUDialog.setMessage("DfuProcessStarting...");
        }


        @Override
        public void onEnablingDfuMode(String deviceAddress) {
            mDFUDialog.setMessage("EnablingDfuMode...");
        }

        @Override
        public void onFirmwareValidating(String deviceAddress) {
            mDFUDialog.setMessage("FirmwareValidating...");
        }

        @Override
        public void onDfuCompleted(String deviceAddress) {
            Toast.makeText(DeviceInfoActivity.this, "DfuCompleted!", Toast.LENGTH_SHORT).show();
            dismissDFUProgressDialog();
        }

        @Override
        public void onDfuAborted(String deviceAddress) {
            mDFUDialog.setMessage("DfuAborted...");
        }

        @Override
        public void onProgressChanged(String deviceAddress, int percent, float speed, float avgSpeed, int currentPart, int partsTotal) {
            mDFUDialog.setMessage("Progress:" + percent + "%");
        }

        @Override
        public void onError(String deviceAddress, int error, int errorType, String message) {
            String errorMessage = String.format("error:%d,errorType:%d,message:%s)", error, errorType, message);
            Toast.makeText(DeviceInfoActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            LogModule.i(message);
            dismissDFUProgressDialog();
        }
    };

    private final DfuLogListener mDfuLogListener = new DfuLogListener() {
        @Override
        public void onLogEvent(String deviceAddress, int level, String message) {
            switch (level) {
                case DfuService.LOG_LEVEL_APPLICATION:
                case DfuService.LOG_LEVEL_DEBUG:
                case DfuService.LOG_LEVEL_VERBOSE:
                case DfuService.LOG_LEVEL_INFO:
                case DfuService.LOG_LEVEL_WARNING:
                case DfuService.LOG_LEVEL_ERROR:
                    LogModule.w(level + ":" + message);
                    break;
            }
        }
    };
}

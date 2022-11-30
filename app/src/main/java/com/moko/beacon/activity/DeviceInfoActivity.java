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
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.elvishew.xlog.XLog;
import com.moko.beacon.BeaconConstants;
import com.moko.beacon.R;
import com.moko.beacon.databinding.ActivityDeviceInfoBinding;
import com.moko.beacon.dialog.AlertMessageDialog;
import com.moko.beacon.dialog.LoadingDialog;
import com.moko.beacon.dialog.LoadingMessageDialog;
import com.moko.beacon.entity.BeaconParam;
import com.moko.beacon.service.DfuService;
import com.moko.beacon.utils.FileUtils;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import no.nordicsemi.android.dfu.DfuLogListener;
import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;


public class DeviceInfoActivity extends BaseActivity {

    public static final int REQUEST_CODE_SELECT_FIRMWARE = 0x10;
    private ActivityDeviceInfoBinding mBind;
    private BeaconParam mBeaconParam;

    private boolean mIsCloseConnectable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityDeviceInfoBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        mBeaconParam = (BeaconParam) getIntent().getSerializableExtra(BeaconConstants.EXTRA_KEY_DEVICE_PARAM);
        if (mBeaconParam == null) {
            finish();
            return;
        }
        mBind.rlIbeaconThreeAxis.setVisibility(!TextUtils.isEmpty(mBeaconParam.threeAxis) ? View.VISIBLE : View.GONE);
        if (MokoSupport.getInstance().isConnDevice(mBeaconParam.iBeaconMAC)) {
            mBind.tvConnState.setText(getString(R.string.device_info_conn_status_disconnect));
            mBind.viewCover.setVisibility(View.GONE);
        } else {
            mBind.tvConnState.setText(getString(R.string.device_info_conn_status_connect));
            mBind.viewCover.setVisibility(View.VISIBLE);
        }
        changeValue();

        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
    }

    private void changeValue() {
        mBind.tvIbeaconBattery.setText(mBeaconParam.battery);
        mBind.tvIbeaconUuid.setText(mBeaconParam.uuid);
        mBind.tvIbeaconMajor.setText(mBeaconParam.major);
        mBind.tvIbeaconMinor.setText(mBeaconParam.minor);
        mBind.tvIbeaconMeasurePower.setText(String.format("-%sdBm", mBeaconParam.measurePower));
        mBind.tvIbeaconTransmission.setText(mBeaconParam.transmission);
        mBind.tvIbeaconBroadcastingInterval.setText(mBeaconParam.broadcastingInterval);
        mBind.tvIbeaconSerialID.setText(mBeaconParam.serialID);
        mBind.tvIbeaconMac.setText(mBeaconParam.iBeaconMAC);
        mBind.tvIbeaconDeviceName.setText(mBeaconParam.iBeaconName);
        boolean isConnectable = "00".equals(mBeaconParam.connectionMode);
        mBind.ivIbeaconDeviceConnMode.setImageDrawable(ContextCompat.getDrawable(this, isConnectable ? R.drawable.connectable_checked : R.drawable.connectable_unchecked));
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
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                mBind.tvConnState.setText(getString(R.string.device_info_conn_status_connect));
                mBind.viewCover.setVisibility(View.VISIBLE);
                ToastUtils.showToast(DeviceInfoActivity.this, "Connect Failed");
                dismissLoadingMessageDialog();
            }
            if (MokoConstants.ACTION_DISCOVER_SUCCESS.equals(action)) {
                mBind.tvConnState.setText(getString(R.string.device_info_conn_status_disconnect));
                mBind.viewCover.setVisibility(View.GONE);
                // 读取全部可读数据
                mBind.tvConnState.postDelayed(() -> {
                    // open password notify and set password
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
                    orderTasks.add(OrderTaskAssembler.getRuntime());
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
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                int responseType = response.responseType;
                byte[] value = response.responseValue;

            }
            if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
            }
            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                mBind.tvConnState.postDelayed(() -> {
                    dismissLoadingMessageDialog();
                }, 500);
            }
            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderCHAR) {
                    case CHAR_BATTERY:
                        mBeaconParam.battery = Integer.parseInt(MokoUtils.bytesToHexString(value), 16) + "";
                        mBind.tvIbeaconBattery.setText(mBeaconParam.battery);
                        break;
                    case CHAR_DEVICE_UUID:
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
                            mBind.tvIbeaconUuid.setText(uuid);
                            mBind.tvIbeaconUuid.setText(mBeaconParam.uuid);
                        }
                        break;
                    case CHAR_MAJOR:
                        mBeaconParam.major = Integer.parseInt(MokoUtils.bytesToHexString(value), 16) + "";
                        mBind.tvIbeaconMajor.setText(mBeaconParam.major);
                        break;
                    case CHAR_MINOR:
                        mBeaconParam.minor = Integer.parseInt(MokoUtils.bytesToHexString(value), 16) + "";
                        mBind.tvIbeaconMinor.setText(mBeaconParam.minor);
                        break;
                    case CHAR_MEASURE_POWER:
                        mBeaconParam.measurePower = Integer.parseInt(MokoUtils.bytesToHexString(value), 16) + "";
                        mBind.tvIbeaconMeasurePower.setText(String.format("-%sdBm", mBeaconParam.measurePower));
                        break;
                    case CHAR_TRANSMISSION:
                        int transmission = Integer.parseInt(MokoUtils.bytesToHexString(value), 16);
                        if (transmission == 8) {
                            transmission = 7;
                        }
                        mBeaconParam.transmission = transmission + "";
                        mBind.tvIbeaconTransmission.setText(mBeaconParam.transmission);
                        break;
                    case CHAR_ADV_INTERVAL:
                        mBeaconParam.broadcastingInterval = Integer.parseInt(MokoUtils.bytesToHexString(value), 16) + "";
                        mBind.tvIbeaconBroadcastingInterval.setText(mBeaconParam.broadcastingInterval);
                        break;
                    case CHAR_SERIAL_ID:
                        mBeaconParam.serialID = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
                        mBind.tvIbeaconSerialID.setText(mBeaconParam.serialID);
                        break;
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
                            mBeaconParam.iBeaconMAC = mac;
                            mBeaconParam.beaconInfo.iBeaconMac = mac;
                            mBind.tvIbeaconMac.setText(mBeaconParam.iBeaconMAC);
                        }
                        break;
                    case CHAR_ADV_NAME:
                        mBeaconParam.iBeaconName = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
                        mBind.tvIbeaconDeviceName.setText(mBeaconParam.iBeaconName);
                        break;
                    case CHAR_CONNECTION:
                        if (responseType == OrderTask.RESPONSE_TYPE_READ) {
                            mBeaconParam.connectionMode = MokoUtils.bytesToHexString(value);
                            boolean isConnectable = "00".equals(mBeaconParam.connectionMode);
                            mBind.ivIbeaconDeviceConnMode.setImageDrawable(ContextCompat.getDrawable(DeviceInfoActivity.this, isConnectable ? R.drawable.connectable_checked : R.drawable.connectable_unchecked));
                            if (mIsCloseConnectable && !isConnectable) {
                                mIsCloseConnectable = false;
                                dismissLoadingMessageDialog();
                                back();
                            }
                        } else {
                            dismissLoadingMessageDialog();
                            mIsCloseConnectable = true;
                            mBeaconParam.connectionMode = null;
                            getEmptyInfo();
                        }
                        break;
                    case CHAR_MANUFACTURER_NAME:
                        mBeaconParam.beaconInfo.firmname = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
                        break;
                    case CHAR_SOFTWARE_REVISION:
                        mBeaconParam.beaconInfo.softVersion = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
                        break;
                    case CHAR_MODEL_NUMBER:
                        mBeaconParam.beaconInfo.deviceName = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
                        break;
                    case CHAR_SERIAL_NUMBER:
                        mBeaconParam.beaconInfo.iBeaconDate = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
                        break;
                    case CHAR_HARDWARE_REVISION:
                        mBeaconParam.beaconInfo.hardwareVersion = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
                        break;
                    case CHAR_FIRMWARE_REVISION:
                        mBeaconParam.beaconInfo.firmwareVersion = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
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
                                    mBeaconParam.beaconInfo.chipModel = MokoUtils.hex2String(MokoUtils.bytesToHexString(chipModelBytes));
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
                                    mBeaconParam.beaconInfo.runtime = String.format("%dD%dh%dm%ds", day, hours, minutes, seconds);
                                    break;
                                case SET_CLOSE:
                                    if ((value[value.length - 1] & 0xff) == 0xAA) {
                                        dismissLoadingMessageDialog();
                                        ToastUtils.showToast(DeviceInfoActivity.this, "Power off successfully");
                                        back();
                                    } else {
                                        ToastUtils.showToast(DeviceInfoActivity.this, "Power off failed");
                                    }
                                    break;
                            }
                        }
                        break;
                    case CHAR_PASSWORD:
                        if (value.length > 1)
                            return;
                        if (value[0] == 0) {
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
                            mBind.tvConnState.setText(getString(R.string.device_info_conn_status_connect));
                            mBind.viewCover.setVisibility(View.VISIBLE);
                            ToastUtils.showToast(DeviceInfoActivity.this, "Connect Failed");
                            dismissLoadingMessageDialog();
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

    private void back() {
        if (MokoSupport.getInstance().isConnDevice(mBeaconParam.iBeaconMAC)) {
            MokoSupport.getInstance().disConnectBle();
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        if (isWindowLocked()) return;
        back();
    }

    public void onBattery(View view) {
        if (isWindowLocked()) return;
        ToastUtils.showToast(this, getString(R.string.device_info_cannot_modify));
    }

    public void onDeviceMac(View view) {
        if (isWindowLocked()) return;
        ToastUtils.showToast(this, getString(R.string.device_info_cannot_modify));
    }

    public void onViewCover(View view) {
        if (isWindowLocked()) return;
        ToastUtils.showToast(this, R.string.disconnect_alert);
    }

    public void onBack(View view) {
        if (isWindowLocked()) return;
        back();
    }

    public void onConnectState(View view) {
        if (isWindowLocked()) return;
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            ToastUtils.showToast(this, "bluetooth is closed,please open");
            return;
        }
        if (!MokoSupport.getInstance().isConnDevice(mBeaconParam.iBeaconMAC)) {
            MokoSupport.getInstance().connDevice(mBeaconParam.iBeaconMAC);
            showLoadingMessageDialog(getString(R.string.dialog_connecting));
        } else {
            mBind.tvConnState.setText(getString(R.string.device_info_conn_status_connect));
            MokoSupport.getInstance().disConnectBle();
            mBind.viewCover.setVisibility(View.VISIBLE);
        }
    }

    public void onUUID(View view) {
        if (isWindowLocked()) return;
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            ToastUtils.showToast(this, "bluetooth is closed,please open");
            return;
        }
        if (!MokoSupport.getInstance().isConnDevice(mBeaconParam.iBeaconMAC)) {
            ToastUtils.showToast(this, getString(R.string.alert_click_reconnect));
            return;
        }
        Intent intent = new Intent(this, SetUUIDActivity.class);
        intent.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_UUID, mBeaconParam.uuid);
        startActivityForResult(intent, BeaconConstants.REQUEST_CODE_SET_UUID);
    }

    public void onMajor(View view) {
        if (isWindowLocked()) return;
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            ToastUtils.showToast(this, "bluetooth is closed,please open");
            return;
        }
        if (!MokoSupport.getInstance().isConnDevice(mBeaconParam.iBeaconMAC)) {
            ToastUtils.showToast(this, getString(R.string.alert_click_reconnect));
            return;
        }
        Intent intent = new Intent(this, SetMajorActivity.class);
        intent.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_MAJOR, Integer.parseInt(mBeaconParam.major));
        startActivityForResult(intent, BeaconConstants.REQUEST_CODE_SET_MAJOR);
    }

    public void onMinor(View view) {
        if (isWindowLocked()) return;
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            ToastUtils.showToast(this, "bluetooth is closed,please open");
            return;
        }
        if (!MokoSupport.getInstance().isConnDevice(mBeaconParam.iBeaconMAC)) {
            ToastUtils.showToast(this, getString(R.string.alert_click_reconnect));
            return;
        }
        Intent intent = new Intent(this, SetMinorActivity.class);
        intent.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_MINOR, Integer.parseInt(mBeaconParam.minor));
        startActivityForResult(intent, BeaconConstants.REQUEST_CODE_SET_MINOR);
    }

    public void onMeasurePower(View view) {
        if (isWindowLocked()) return;
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            ToastUtils.showToast(this, "bluetooth is closed,please open");
            return;
        }
        if (!MokoSupport.getInstance().isConnDevice(mBeaconParam.iBeaconMAC)) {
            ToastUtils.showToast(this, getString(R.string.alert_click_reconnect));
            return;
        }
        Intent intent = new Intent(this, SetMeasurePowerActivity.class);
        intent.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_MEASURE_POWER, Integer.parseInt(mBeaconParam.measurePower));
        startActivityForResult(intent, BeaconConstants.REQUEST_CODE_SET_MEASURE_POWER);
    }

    public void onTransmissionPower(View view) {
        if (isWindowLocked()) return;
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            ToastUtils.showToast(this, "bluetooth is closed,please open");
            return;
        }
        if (!MokoSupport.getInstance().isConnDevice(mBeaconParam.iBeaconMAC)) {
            ToastUtils.showToast(this, getString(R.string.alert_click_reconnect));
            return;
        }
        Intent intent = new Intent(this, SetTransmissionActivity.class);
        intent.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_TRANSMISSION, Integer.parseInt(mBeaconParam.transmission));
        startActivityForResult(intent, BeaconConstants.REQUEST_CODE_SET_TRANSMISSION);
    }

    public void onAdvInterval(View view) {
        if (isWindowLocked()) return;
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            ToastUtils.showToast(this, "bluetooth is closed,please open");
            return;
        }
        if (!MokoSupport.getInstance().isConnDevice(mBeaconParam.iBeaconMAC)) {
            ToastUtils.showToast(this, getString(R.string.alert_click_reconnect));
            return;
        }
        Intent intent = new Intent(this, SetBroadcastIntervalActivity.class);
        intent.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_BROADCASTINTERVAL, Integer.parseInt(mBeaconParam.broadcastingInterval));
        startActivityForResult(intent, BeaconConstants.REQUEST_CODE_SET_BROADCASTINTERVAL);
    }

    public void onSerialId(View view) {
        if (isWindowLocked()) return;
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            ToastUtils.showToast(this, "bluetooth is closed,please open");
            return;
        }
        if (!MokoSupport.getInstance().isConnDevice(mBeaconParam.iBeaconMAC)) {
            ToastUtils.showToast(this, getString(R.string.alert_click_reconnect));
            return;
        }
        Intent intent = new Intent(this, SetDeviceIdActivity.class);
        intent.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_DEVICE_ID, mBeaconParam.serialID);
        startActivityForResult(intent, BeaconConstants.REQUEST_CODE_SET_DEVICE_ID);
    }

    public void onIBeaconName(View view) {
        if (isWindowLocked()) return;
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            ToastUtils.showToast(this, "bluetooth is closed,please open");
            return;
        }
        if (!MokoSupport.getInstance().isConnDevice(mBeaconParam.iBeaconMAC)) {
            ToastUtils.showToast(this, getString(R.string.alert_click_reconnect));
            return;
        }
        Intent intent = new Intent(this, SetIBeaconNameActivity.class);
        intent.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_IBEACON_NAME, mBeaconParam.iBeaconName);
        intent.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_IBEACON_THREE_AXIS, mBeaconParam.threeAxis);
        startActivityForResult(intent, BeaconConstants.REQUEST_CODE_SET_IBEACON_NAME);
    }

    public void onModifyPassword(View view) {
        if (isWindowLocked()) return;
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            ToastUtils.showToast(this, "bluetooth is closed,please open");
            return;
        }
        if (!MokoSupport.getInstance().isConnDevice(mBeaconParam.iBeaconMAC)) {
            ToastUtils.showToast(this, getString(R.string.alert_click_reconnect));
            return;
        }
        Intent intent = new Intent(this, SetPasswordActivity.class);
        startActivityForResult(intent, BeaconConstants.REQUEST_CODE_SET_PASSWORD);
    }

    public void onAxis(View view) {
        if (isWindowLocked()) return;
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            ToastUtils.showToast(this, "bluetooth is closed,please open");
            return;
        }
        if (!MokoSupport.getInstance().isConnDevice(mBeaconParam.iBeaconMAC)) {
            ToastUtils.showToast(this, getString(R.string.alert_click_reconnect));
            return;
        }
        Intent intent = new Intent(this, ThreeAxesActivity.class);
        startActivityForResult(intent, BeaconConstants.REQUEST_CODE_SET_THREE_AXIS);
    }

    public void onDeviceInfo(View view) {
        if (isWindowLocked()) return;
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            ToastUtils.showToast(this, "bluetooth is closed,please open");
            return;
        }
        if (!MokoSupport.getInstance().isConnDevice(mBeaconParam.iBeaconMAC)) {
            ToastUtils.showToast(this, getString(R.string.alert_click_reconnect));
            return;
        }
        Intent intent = new Intent(this, SystemInfoActivity.class);
        intent.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_INFO, mBeaconParam.beaconInfo);
        startActivityForResult(intent, BeaconConstants.REQUEST_CODE_SET_SYSTEM_INFO);
    }

    public void onConnectable(View view) {
        if (isWindowLocked()) return;
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            ToastUtils.showToast(this, "bluetooth is closed,please open");
            return;
        }
        if (!MokoSupport.getInstance().isConnDevice(mBeaconParam.iBeaconMAC)) {
            ToastUtils.showToast(this, getString(R.string.alert_click_reconnect));
            return;
        }
        final boolean isConnectable = "00".equals(mBeaconParam.connectionMode);
        String connectMessage = String.format("Are you sure to make device %s?", isConnectable ? "disconnectable" : "connectable");
        AlertMessageDialog connectDialog = new AlertMessageDialog();
        connectDialog.setTitle("Warning!");
        connectDialog.setMessage(connectMessage);
        connectDialog.setOnAlertConfirmListener(() -> {
            showLoadingMessageDialog("Syncing...");
            String connectMode = !isConnectable ? "00" : "01";
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setConnection(connectMode));
        });
        connectDialog.show(getSupportFragmentManager());
    }

    public void onPowerOff(View view) {
        if (isWindowLocked()) return;
        AlertMessageDialog powerDialog = new AlertMessageDialog();
        powerDialog.setTitle("Warning!");
        powerDialog.setMessage("Are you sure to turn off the device?Please make sure the device has a button to turn on!");
        powerDialog.setOnAlertConfirmListener(() -> {
            showLoadingMessageDialog("Syncing...");
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setClose());
        });
        powerDialog.show(getSupportFragmentManager());
    }

    public void onDFU(View view) {
        if (isWindowLocked()) return;
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            ToastUtils.showToast(this, "bluetooth is closed,please open");
            return;
        }
        if (!MokoSupport.getInstance().isConnDevice(mBeaconParam.iBeaconMAC)) {
            ToastUtils.showToast(this, getString(R.string.alert_click_reconnect));
            return;
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "select file first!"), REQUEST_CODE_SELECT_FIRMWARE);
        } catch (ActivityNotFoundException ex) {
            ToastUtils.showToast(this, "install file manager app");
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
            mBind.tvConnState.setText(getString(R.string.device_info_conn_status_connect));
            mBind.viewCover.setVisibility(View.VISIBLE);
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
                        if (!MokoSupport.getInstance().isConnDevice(mBeaconParam.iBeaconMAC)) {
                            ToastUtils.showToast(this, getString(R.string.alert_click_reconnect));
                            return;
                        }
                        //得到uri，后面就是将uri转化成file的过程。
                        Uri uri = data.getData();
                        String firmwareFilePath = FileUtils.getPath(this, uri);
                        if (TextUtils.isEmpty(firmwareFilePath))
                            return;
                        final File firmwareFile = new File(firmwareFilePath);
                        if (!firmwareFile.exists() || !firmwareFilePath.toLowerCase().endsWith("zip") || firmwareFile.length() == 0) {
                            ToastUtils.showToast(this, "File error!");
                            return;
                        }
                        final DfuServiceInitiator starter = new DfuServiceInitiator(mBeaconParam.iBeaconMAC)
                                .setDeviceName(mBeaconParam.iBeaconName)
                                .setKeepBond(false)
                                .setDisableNotification(true);
                        starter.setZip(null, firmwareFilePath);
                        starter.start(this, DfuService.class);
                        showDFUProgressDialog("Waiting...");
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
            showLoadingMessageDialog("Syncing...");
            mBind.tvConnState.postDelayed(new Runnable() {
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
            XLog.w("onDeviceConnecting...");
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
            XLog.w("onDeviceDisconnecting...");
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
            XLog.i(message);
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
                    XLog.w(level + ":" + message);
                    break;
            }
        }
    };


    private LoadingDialog mLoadingDialog;

//    private void showLoadingProgressDialog() {
//        mLoadingDialog = new LoadingDialog();
//        mLoadingDialog.show(getSupportFragmentManager());
//
//    }
//
//    private void dismissLoadingProgressDialog() {
//        if (mLoadingDialog != null)
//            mLoadingDialog.dismissAllowingStateLoss();
//    }

    private LoadingMessageDialog mLoadingMessageDialog;

    private void showLoadingMessageDialog(String msg) {
        mLoadingMessageDialog = new LoadingMessageDialog();
        mLoadingMessageDialog.setMessage(msg);
        mLoadingMessageDialog.show(getSupportFragmentManager());

    }

    private void dismissLoadingMessageDialog() {
        if (mLoadingMessageDialog != null)
            mLoadingMessageDialog.dismissAllowingStateLoss();
    }
}

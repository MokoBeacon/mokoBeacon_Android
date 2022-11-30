package com.moko.beacon.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RadioGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.elvishew.xlog.XLog;
import com.moko.beacon.BeaconConstants;
import com.moko.beacon.R;
import com.moko.beacon.adapter.BeaconListAdapter;
import com.moko.beacon.databinding.ActivityMainBinding;
import com.moko.beacon.dialog.LoadingDialog;
import com.moko.beacon.dialog.PasswordDialog;
import com.moko.beacon.entity.BeaconDeviceInfo;
import com.moko.beacon.entity.BeaconInfo;
import com.moko.beacon.entity.BeaconParam;
import com.moko.beacon.utils.BeaconInfoParseableImpl;
import com.moko.beacon.utils.ToastUtils;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.MokoBleScanner;
import com.moko.support.MokoSupport;
import com.moko.support.OrderTaskAssembler;
import com.moko.support.callback.MokoScanDeviceCallback;
import com.moko.support.entity.DeviceInfo;
import com.moko.support.entity.OrderCHAR;
import com.moko.support.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import androidx.annotation.IdRes;
import androidx.recyclerview.widget.LinearLayoutManager;

public class MainActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener, MokoScanDeviceCallback, BaseQuickAdapter.OnItemClickListener {

    public static final int SORT_TYPE_RSSI = 0;
    public static final int SORT_TYPE_MAJOR = 1;
    public static final int SORT_TYPE_MINOR = 2;

    private ActivityMainBinding mBind;

    private Animation animation = null;
    private MokoBleScanner mokoBleScanner;
    private BeaconListAdapter mAdapter;
    private ArrayList<BeaconInfo> mBeaconInfos;
    private boolean isPasswordError;
    private int mSortType;
    private String mFilterText;
    private ConcurrentHashMap<String, BeaconInfo> beaconInfoHashMap;
    private BeaconInfoParseableImpl beaconInfoParseable;
    public String mSavedPassword;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        beaconInfoHashMap = new ConcurrentHashMap<>();
        mBeaconInfos = new ArrayList<>();
        mAdapter = new BeaconListAdapter();
        mAdapter.replaceData(mBeaconInfos);
        mAdapter.setOnItemClickListener(this);
        mAdapter.openLoadAnimation();
        mBind.rvDeviceList.setLayoutManager(new LinearLayoutManager(this));
        mBind.rvDeviceList.setAdapter(mAdapter);
        mBind.rgDeviceSort.setOnCheckedChangeListener(this);
        mBind.rbSortRssi.setChecked(true);
        mBind.etDeviceFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mFilterText = s.toString();
            }
        });

        mHandler = new Handler(Looper.getMainLooper());
        mokoBleScanner = new MokoBleScanner(this);
        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            MokoSupport.getInstance().enableBluetooth();
        } else {
            if (animation == null) {
                startScan();
            }
        }
        mBind.ivRefresh.setOnClickListener(v -> {
            if (!MokoSupport.getInstance().isBluetoothOpen()) {
                MokoSupport.getInstance().enableBluetooth();
                return;
            }
            if (animation == null) {
                startScan();
            } else {
                mHandler.removeMessages(0);
                mokoBleScanner.stopScanDevice();
            }
        });
        mBind.ivAbout.setOnClickListener(v -> {
            startActivity(new Intent(this, AboutActivity.class));
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        String action = event.getAction();
        if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
            mPassword = "";
            dismissLoadingProgressDialog();
            if (isPasswordError) {
                isPasswordError = false;
            } else {
                ToastUtils.showToast(MainActivity.this, "connect failed");
            }
            if (animation == null) {
                startScan();
            }
        }
        if (MokoConstants.ACTION_DISCOVER_SUCCESS.equals(action)) {
            mBeaconParam = new BeaconParam();
            BeaconDeviceInfo beaconInfo = new BeaconDeviceInfo();
            mBeaconParam.beaconInfo = beaconInfo;
            mBeaconParam.threeAxis = mThreeAxis;
            // 读取全部可读数据
            mHandler.postDelayed(() -> {
                mBeaconParam.password = mPassword;
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
                orderTasks.add(OrderTaskAssembler.setPassword(mPassword));
                MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
            }, 500);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        final String action = event.getAction();
        if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
            OrderTaskResponse response = event.getResponse();
            OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
            int responseType = response.responseType;
            byte[] value = response.responseValue;
            switch (orderCHAR) {
                case CHAR_PASSWORD:
                    isPasswordError = true;
                    // 修改密码超时
                    dismissLoadingProgressDialog();
                    ToastUtils.showToast(MainActivity.this, "password error");
                    if (animation == null) {
                        startScan();
                    }
                    break;
            }
        }
        if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
        }
        if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
            OrderTaskResponse response = event.getResponse();
            OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
            int responseType = response.responseType;
            byte[] value = response.responseValue;
            switch (orderCHAR) {
                case CHAR_BATTERY:
                    mBeaconParam.battery = Integer.parseInt(MokoUtils.bytesToHexString(value), 16) + "";
                    break;
                case CHAR_DEVICE_UUID:
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
                    }
                    break;
                case CHAR_MAJOR:
                    mBeaconParam.major = Integer.parseInt(MokoUtils.bytesToHexString(value), 16) + "";
                    break;
                case CHAR_MINOR:
                    mBeaconParam.minor = Integer.parseInt(MokoUtils.bytesToHexString(value), 16) + "";
                    break;
                case CHAR_MEASURE_POWER:
                    mBeaconParam.measurePower = Integer.parseInt(MokoUtils.bytesToHexString(value), 16) + "";
                    break;
                case CHAR_TRANSMISSION:
                    int transmission = Integer.parseInt(MokoUtils.bytesToHexString(value), 16);
                    if (transmission == 8) {
                        transmission = 7;
                    }
                    mBeaconParam.transmission = transmission + "";
                    break;
                case CHAR_ADV_INTERVAL:
                    mBeaconParam.broadcastingInterval = Integer.parseInt(MokoUtils.bytesToHexString(value), 16) + "";
                    break;
                case CHAR_SERIAL_ID:
                    mBeaconParam.serialID = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
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
                    }
                    break;
                case CHAR_ADV_NAME:
                    mBeaconParam.iBeaconName = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
                    break;
                case CHAR_CONNECTION:
                    mBeaconParam.connectionMode = MokoUtils.bytesToHexString(value);
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
                        }
                    }
                    break;
                case CHAR_PASSWORD:
                    if (value.length > 1)
                        return;
                    if (value[0] == 0) {
                        mHandler.postDelayed(() -> {
                            dismissLoadingProgressDialog();
                            XLog.i(mBeaconParam.toString());
                            mSavedPassword = mPassword;
                            Intent deviceInfoIntent = new Intent(MainActivity.this, DeviceInfoActivity.class);
                            deviceInfoIntent.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_PARAM, mBeaconParam);
                            startActivityForResult(deviceInfoIntent, BeaconConstants.REQUEST_CODE_DEVICE_INFO);
                        }, 1000);
                    } else {
                        isPasswordError = true;
                        dismissLoadingProgressDialog();
                        ToastUtils.showToast(MainActivity.this, "password error");
                        if (animation == null) {
                            startScan();
                        }
                    }
                    break;
            }
        }
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
                            if (animation != null) {
                                mHandler.removeMessages(0);
                                mokoBleScanner.stopScanDevice();
                                onStopScan();
                            }
                            break;
                        case BluetoothAdapter.STATE_ON:
                            if (animation == null) {
                                startScan();
                            }
                            break;
                    }
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BeaconConstants.REQUEST_CODE_DEVICE_INFO) {
            if (animation == null) {
                startScan();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        switch (checkedId) {
            case R.id.rb_sort_rssi:
                mSortType = SORT_TYPE_RSSI;
                break;

            case R.id.rb_sort_major:
                mSortType = SORT_TYPE_MAJOR;
                break;

            case R.id.rb_sort_minor:
                mSortType = SORT_TYPE_MINOR;
                break;

        }
    }

    private void startScan() {
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            MokoSupport.getInstance().enableBluetooth();
            return;
        }
        animation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
        mBind.ivRefresh.startAnimation(animation);
        beaconInfoParseable = new BeaconInfoParseableImpl();
        mokoBleScanner.startScanDevice(this);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mokoBleScanner.stopScanDevice();
            }
        }, 1000 * 60);
    }

    @Override
    public void onStartScan() {
        beaconInfoHashMap.clear();
        new Thread(() -> {
            while (animation != null) {
                runOnUiThread(() -> {
                    mAdapter.replaceData(mBeaconInfos);
                    mBind.tvDevicesTitle.setText(getString(R.string.device_list_title_num, mBeaconInfos.size()));
                });
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                updateDevices();
            }
        }).start();
    }

    @Override
    public void onScanDevice(DeviceInfo deviceInfo) {
        BeaconInfo beaconInfo = beaconInfoParseable.parseDeviceInfo(deviceInfo);
        if (beaconInfo == null)
            return;
        beaconInfoHashMap.put(beaconInfo.mac, beaconInfo);
    }

    @Override
    public void onStopScan() {
        mBind.ivRefresh.clearAnimation();
        animation = null;
    }

    private void updateDevices() {
        mBeaconInfos.clear();
        if (!TextUtils.isEmpty(mFilterText)) {
            ArrayList<BeaconInfo> beaconXInfosFilter = new ArrayList<>(beaconInfoHashMap.values());
            Iterator<BeaconInfo> iterator = beaconXInfosFilter.iterator();
            while (iterator.hasNext()) {
                BeaconInfo beaconInfo = iterator.next();
                if (!TextUtils.isEmpty(mFilterText) && (!TextUtils.isEmpty(beaconInfo.name) && (!TextUtils.isEmpty(beaconInfo.mac))
                        && (beaconInfo.name.toLowerCase().contains(mFilterText.toLowerCase()) || beaconInfo.mac.toLowerCase().replaceAll(":", "").contains(mFilterText.toLowerCase())))) {
                    continue;
                } else {
                    iterator.remove();
                }
            }
            mBeaconInfos.addAll(beaconXInfosFilter);
        } else {
            mBeaconInfos.addAll(beaconInfoHashMap.values());
        }
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        // 排序
        switch (mSortType) {
            case SORT_TYPE_RSSI:
                if (!mBeaconInfos.isEmpty()) {
                    Collections.sort(mBeaconInfos, (lhs, rhs) -> {
                        if (lhs.rssi > rhs.rssi) {
                            return -1;
                        } else if (lhs.rssi < rhs.rssi) {
                            return 1;
                        }
                        return 0;
                    });
                }
                break;
            case SORT_TYPE_MAJOR:
                if (!mBeaconInfos.isEmpty()) {
                    Collections.sort(mBeaconInfos, (lhs, rhs) -> {
                        if (lhs.major > rhs.major) {
                            return -1;
                        } else if (lhs.major < rhs.major) {
                            return 1;
                        }
                        return 0;
                    });
                }
                break;
            case SORT_TYPE_MINOR:
                if (!mBeaconInfos.isEmpty()) {
                    Collections.sort(mBeaconInfos, (lhs, rhs) -> {
                        if (lhs.minor > rhs.minor) {
                            return -1;
                        } else if (lhs.minor < rhs.minor) {
                            return 1;
                        }
                        return 0;
                    });
                }
                break;
        }
    }

    private BeaconParam mBeaconParam;
    private String mPassword;
    private String mThreeAxis;


    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            MokoSupport.getInstance().enableBluetooth();
            return;
        }
        final BeaconInfo beaconInfo = (BeaconInfo) adapter.getItem(position);
        if (beaconInfo != null && !isFinishing()) {
            XLog.i(beaconInfo.toString());
            if (animation != null) {
                mHandler.removeMessages(0);
                mokoBleScanner.stopScanDevice();
            }
            // show password
            final PasswordDialog dialog = new PasswordDialog(MainActivity.this);
            dialog.setData(mSavedPassword);
            dialog.setOnPasswordClicked(new PasswordDialog.PasswordClickListener() {
                @Override
                public void onEnsureClicked(String password) {
                    if (!MokoSupport.getInstance().isBluetoothOpen()) {
                        MokoSupport.getInstance().enableBluetooth();
                        return;
                    }
                    XLog.i(password);
                    mPassword = password;
                    mThreeAxis = beaconInfo.threeAxis;
                    if (animation != null) {
                        mHandler.removeMessages(0);
                        mokoBleScanner.stopScanDevice();
                    }
                    showLoadingProgressDialog();
                    mBind.ivRefresh.postDelayed(() -> MokoSupport.getInstance().connDevice(beaconInfo.mac), 500);
                }

                @Override
                public void onDismiss() {

                }
            });
            dialog.show();
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    runOnUiThread(() -> dialog.showKeyboard());
                }
            }, 200);
        }
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

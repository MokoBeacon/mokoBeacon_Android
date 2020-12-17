package com.moko.beacon.activity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.moko.beacon.BeaconConstants;
import com.moko.beacon.R;
import com.moko.beacon.adapter.BeaconListAdapter;
import com.moko.beacon.dialog.PasswordDialog;
import com.moko.beacon.entity.BeaconDeviceInfo;
import com.moko.beacon.entity.BeaconInfo;
import com.moko.beacon.entity.BeaconParam;
import com.moko.beacon.utils.BeaconInfoParseableImpl;
import com.moko.beacon.utils.ToastUtils;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.OrderTaskAssembler;
import com.moko.support.callback.MokoScanDeviceCallback;
import com.moko.support.entity.DeviceInfo;
import com.moko.support.entity.OrderType;
import com.moko.support.event.ConnectStatusEvent;
import com.moko.support.event.OrderTaskResponseEvent;
import com.moko.support.handler.BaseMessageHandler;
import com.moko.support.log.LogModule;
import com.moko.support.task.OrderTask;
import com.moko.support.task.OrderTaskResponse;
import com.moko.support.utils.MokoUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.IdRes;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @Date 2017/12/7 0007
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beacon.activity.MainActivity
 */
public class MainActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener, MokoScanDeviceCallback, AdapterView.OnItemClickListener {

    public static final int SORT_TYPE_RSSI = 0;
    public static final int SORT_TYPE_MAJOR = 1;
    public static final int SORT_TYPE_MINOR = 2;

    @BindView(R.id.et_device_filter)
    EditText etDeviceFilter;
    @BindView(R.id.rb_sort_rssi)
    RadioButton rbSortRssi;
    @BindView(R.id.rb_sort_major)
    RadioButton rbSortMajor;
    @BindView(R.id.rb_sort_minor)
    RadioButton rbSortMinor;
    @BindView(R.id.rg_device_sort)
    RadioGroup rgDeviceSort;
    @BindView(R.id.lv_device_list)
    ListView lvDeviceList;
    @BindView(R.id.iv_refresh)
    ImageView ivRefresh;
    @BindView(R.id.tv_devices_title)
    TextView tvDevicesTitle;


    private Animation animation = null;
    private BeaconListAdapter mAdapter;
    private ArrayList<BeaconInfo> mBeaconInfos;
    private int mSortType;
    private String mFilterText;
    private HashMap<String, BeaconInfo> beaconMap;
    private BeaconInfoParseableImpl beaconInfoParseable;
    public String mSavedPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mAdapter = new BeaconListAdapter(this);
        mBeaconInfos = new ArrayList<>();
        beaconMap = new HashMap<>();
        mAdapter.setItems(mBeaconInfos);
        lvDeviceList.setAdapter(mAdapter);
        rgDeviceSort.setOnCheckedChangeListener(this);
        lvDeviceList.setOnItemClickListener(this);
        rbSortRssi.setChecked(true);
        etDeviceFilter.addTextChangedListener(new TextWatcher() {
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

        mHandler = new CunstomHandler(this);
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
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        String action = event.getAction();
        if (MokoConstants.ACTION_CONN_STATUS_DISCONNECTED.equals(action)) {
            dismissLoadingProgressDialog();
            ToastUtils.showToast(MainActivity.this, "connect failed");
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
            OrderType orderType = response.orderType;
            int responseType = response.responseType;
            byte[] value = response.responseValue;
            switch (orderType) {
                case PASSWORD:
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
            OrderType orderType = response.orderType;
            int responseType = response.responseType;
            byte[] value = response.responseValue;
            switch (orderType) {
                case BATTERY:
                    mBeaconParam.battery = Integer.parseInt(MokoUtils.bytesToHexString(value), 16) + "";
                    break;
                case DEVICE_UUID:
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
                case MAJOR:
                    mBeaconParam.major = Integer.parseInt(MokoUtils.bytesToHexString(value), 16) + "";
                    break;
                case MINOR:
                    mBeaconParam.minor = Integer.parseInt(MokoUtils.bytesToHexString(value), 16) + "";
                    break;
                case MEASURE_POWER:
                    mBeaconParam.measurePower = Integer.parseInt(MokoUtils.bytesToHexString(value), 16) + "";
                    break;
                case TRANSMISSION:
                    int transmission = Integer.parseInt(MokoUtils.bytesToHexString(value), 16);
                    if (transmission == 8) {
                        transmission = 7;
                    }
                    mBeaconParam.transmission = transmission + "";
                    break;
                case ADV_INTERVAL:
                    mBeaconParam.broadcastingInterval = Integer.parseInt(MokoUtils.bytesToHexString(value), 16) + "";
                    break;
                case SERIAL_ID:
                    mBeaconParam.serialID = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
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
                    }
                    break;
                case ADV_NAME:
                    mBeaconParam.iBeaconName = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
                    break;
                case CONNECTION:
                    mBeaconParam.connectionMode = MokoUtils.bytesToHexString(value);
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
                    break;
                case PASSWORD:
                    if ("00".equals(MokoUtils.bytesToHexString(value))) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dismissLoadingProgressDialog();
                                LogModule.i(mBeaconParam.toString());
                                mSavedPassword = mPassword;
                                Intent deviceInfoIntent = new Intent(MainActivity.this, DeviceInfoActivity.class);
                                deviceInfoIntent.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_PARAM, mBeaconParam);
                                startActivityForResult(deviceInfoIntent, BeaconConstants.REQUEST_CODE_DEVICE_INFO);
                            }
                        }, 1000);
                    } else {
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
                                MokoSupport.getInstance().stopScanDevice();
                                onStopScan();
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
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case MokoConstants.REQUEST_CODE_ENABLE_BT:
                    if (animation == null) {
                        startScan();
                    }
                    break;

            }
        } else {
            switch (requestCode) {
                case MokoConstants.REQUEST_CODE_ENABLE_BT:
                    // 未打开蓝牙
                    MainActivity.this.finish();
                    break;
                case BeaconConstants.REQUEST_CODE_DEVICE_INFO:
                    if (animation == null) {
                        startScan();
                    }
                    break;
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

    @OnClick({R.id.iv_about, R.id.iv_refresh})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.iv_refresh:
                if (!MokoSupport.getInstance().isBluetoothOpen()) {
                    // 蓝牙未打开，开启蓝牙
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, MokoConstants.REQUEST_CODE_ENABLE_BT);
                    return;
                }
                if (animation == null) {
                    startScan();
                } else {
                    mHandler.removeMessages(0);
                    MokoSupport.getInstance().stopScanDevice();
                }
                break;
        }
    }

    private void startScan() {
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, MokoConstants.REQUEST_CODE_ENABLE_BT);
            return;
        }
        animation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
        ivRefresh.startAnimation(animation);
        beaconInfoParseable = new BeaconInfoParseableImpl();
        if (!isLocationPermissionOpen()) {
            return;
        }
        MokoSupport.getInstance().startScanDevice(this);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MokoSupport.getInstance().stopScanDevice();
            }
        }, 1000 * 60);
    }

    @Override
    public void onStartScan() {
        beaconMap.clear();
    }

    @Override
    public void onScanDevice(DeviceInfo device) {
        BeaconInfo beaconInfo = beaconInfoParseable.parseDeviceInfo(device);
        if (beaconInfo == null) {
            return;
        }
        beaconMap.put(beaconInfo.mac, beaconInfo);
        updateDevices();
    }

    @Override
    public void onStopScan() {
        findViewById(R.id.iv_refresh).clearAnimation();
        animation = null;
        updateDevices();
    }

    private void updateDevices() {
        mBeaconInfos.clear();
        if (!TextUtils.isEmpty(mFilterText)) {
            ArrayList<BeaconInfo> beaconXInfosFilter = new ArrayList<>(beaconMap.values());
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
            mBeaconInfos.addAll(beaconMap.values());
        }
        // 排序
        switch (mSortType) {
            case SORT_TYPE_RSSI:
                if (!mBeaconInfos.isEmpty()) {
                    Collections.sort(mBeaconInfos, new Comparator<BeaconInfo>() {
                        @Override
                        public int compare(BeaconInfo lhs, BeaconInfo rhs) {
                            if (lhs.rssi > rhs.rssi) {
                                return -1;
                            } else if (lhs.rssi < rhs.rssi) {
                                return 1;
                            }
                            return 0;
                        }
                    });
                }
                break;
            case SORT_TYPE_MAJOR:
                if (!mBeaconInfos.isEmpty()) {
                    Collections.sort(mBeaconInfos, new Comparator<BeaconInfo>() {
                        @Override
                        public int compare(BeaconInfo lhs, BeaconInfo rhs) {
                            if (lhs.major > rhs.major) {
                                return -1;
                            } else if (lhs.major < rhs.major) {
                                return 1;
                            }
                            return 0;
                        }
                    });
                }
                break;
            case SORT_TYPE_MINOR:
                if (!mBeaconInfos.isEmpty()) {
                    Collections.sort(mBeaconInfos, new Comparator<BeaconInfo>() {
                        @Override
                        public int compare(BeaconInfo lhs, BeaconInfo rhs) {
                            if (lhs.minor > rhs.minor) {
                                return -1;
                            } else if (lhs.minor < rhs.minor) {
                                return 1;
                            }
                            return 0;
                        }
                    });
                }
                break;
        }
        mAdapter.notifyDataSetChanged();
        tvDevicesTitle.setText(getString(R.string.device_list_title_num, mBeaconInfos.size()));
    }

    private BeaconParam mBeaconParam;
    private String mPassword;
    private String mThreeAxis;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, MokoConstants.REQUEST_CODE_ENABLE_BT);
            return;
        }
        final BeaconInfo beaconInfo = (BeaconInfo) parent.getItemAtPosition(position);
        if (beaconInfo != null && !isFinishing()) {
            LogModule.i(beaconInfo.toString());
            if (animation != null) {
                mHandler.removeMessages(0);
                MokoSupport.getInstance().stopScanDevice();
            }
            final PasswordDialog dialog = new PasswordDialog(this);
            dialog.setSavedPassword(mSavedPassword);
            dialog.setOnPasswordClicked(new PasswordDialog.PasswordClickListener() {
                @Override
                public void onEnsureClicked(String password) {
                    if (!MokoSupport.getInstance().isBluetoothOpen()) {
                        // 蓝牙未打开，开启蓝牙
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, MokoConstants.REQUEST_CODE_ENABLE_BT);
                        return;
                    }
                    LogModule.i(password);
                    mPassword = password;
                    mThreeAxis = beaconInfo.threeAxis;
                    MokoSupport.getInstance().connDevice(MainActivity.this, beaconInfo.mac);
                    showLoadingProgressDialog();
                }

                @Override
                public void onDismiss() {
                    if (animation == null) {
                        startScan();
                    }
                }
            });
            dialog.show();
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.showKeyboard();
                        }
                    });
                }
            }, 200);
        }
    }

    private ProgressDialog mLoadingDialog;

    private void showLoadingProgressDialog() {
        mLoadingDialog = new ProgressDialog(MainActivity.this);
        mLoadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mLoadingDialog.setCanceledOnTouchOutside(false);
        mLoadingDialog.setCancelable(false);
        mLoadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mLoadingDialog.setMessage(getString(R.string.dialog_connecting));
        if (!isFinishing() && mLoadingDialog != null && !mLoadingDialog.isShowing()) {
            mLoadingDialog.show();
        }
    }

    private void dismissLoadingProgressDialog() {
        if (!isFinishing() && mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    public CunstomHandler mHandler;

    public class CunstomHandler extends BaseMessageHandler<MainActivity> {

        public CunstomHandler(MainActivity activity) {
            super(activity);
        }

        @Override
        protected void handleMessage(MainActivity activity, Message msg) {
        }
    }
}

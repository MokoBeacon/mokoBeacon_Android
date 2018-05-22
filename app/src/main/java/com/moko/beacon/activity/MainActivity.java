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
import android.support.annotation.IdRes;
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
import com.moko.beacon.service.MokoService;
import com.moko.beacon.utils.BeaconInfoParseableImpl;
import com.moko.beacon.utils.ToastUtils;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.callback.MokoScanDeviceCallback;
import com.moko.support.entity.DeviceInfo;
import com.moko.support.entity.OrderType;
import com.moko.support.log.LogModule;
import com.moko.support.utils.MokoUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
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
    @Bind(R.id.et_device_filter)
    EditText etDeviceFilter;
    @Bind(R.id.rb_sort_rssi)
    RadioButton rbSortRssi;
    @Bind(R.id.rb_sort_major)
    RadioButton rbSortMajor;
    @Bind(R.id.rb_sort_minor)
    RadioButton rbSortMinor;
    @Bind(R.id.rg_device_sort)
    RadioGroup rgDeviceSort;
    @Bind(R.id.lv_device_list)
    ListView lvDeviceList;
    @Bind(R.id.iv_refresh)
    ImageView ivRefresh;
    @Bind(R.id.tv_devices_title)
    TextView tvDevicesTitle;


    private Animation animation = null;
    private BeaconListAdapter mAdapter;
    private ArrayList<BeaconInfo> mBeaconInfos;
    private int mSortType;
    private String mFilterText;
    private MokoService mMokoService;
    private HashMap<String, BeaconInfo> beaconMap;
    private BeaconInfoParseableImpl beaconInfoParseable;

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
        bindService(new Intent(this, MokoService.class), mServiceConnection, BIND_AUTO_CREATE);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMokoService = ((MokoService.LocalBinder) service).getService();
            // 注册广播接收器
            IntentFilter filter = new IntentFilter();
            filter.addAction(MokoConstants.ACTION_CONNECT_SUCCESS);
            filter.addAction(MokoConstants.ACTION_CONNECT_DISCONNECTED);
            filter.addAction(MokoConstants.ACTION_RESPONSE_SUCCESS);
            filter.addAction(MokoConstants.ACTION_RESPONSE_TIMEOUT);
            filter.addAction(MokoConstants.ACTION_RESPONSE_FINISH);
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            filter.setPriority(100);
            registerReceiver(mReceiver, filter);
            if (animation == null) {
                startScan();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (MokoConstants.ACTION_CONNECT_SUCCESS.equals(action)) {
                    mBeaconParam = new BeaconParam();
                    BeaconDeviceInfo beaconInfo = new BeaconDeviceInfo();
                    mBeaconParam.beaconInfo = beaconInfo;
                    mBeaconParam.threeAxis = mThreeAxis;
                    // 读取全部可读数据
                    mMokoService.mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mBeaconParam.password = mPassword;
                            mMokoService.getReadableData(mPassword);
                        }
                    }, 1000);
                }
                if (MokoConstants.ACTION_CONNECT_DISCONNECTED.equals(action)) {
                    dismissLoadingProgressDialog();
                    ToastUtils.showToast(MainActivity.this, "connect failed");
                    if (animation == null) {
                        startScan();
                    }
                }
                if (MokoConstants.ACTION_RESPONSE_TIMEOUT.equals(action)) {
                    OrderType orderType = (OrderType) intent.getSerializableExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TYPE);
                    switch (orderType) {
                        case changePassword:
                            // 修改密码超时
                            dismissLoadingProgressDialog();
                            ToastUtils.showToast(MainActivity.this, "password error");
                            if (animation == null) {
                                startScan();
                            }
                            break;
                    }
                }
                if (MokoConstants.ACTION_RESPONSE_FINISH.equals(action)) {
                }
                if (MokoConstants.ACTION_RESPONSE_SUCCESS.equals(action)) {
                    OrderType orderType = (OrderType) intent.getSerializableExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TYPE);
                    byte[] value = intent.getByteArrayExtra(MokoConstants.EXTRA_KEY_RESPONSE_VALUE);
                    switch (orderType) {
                        case battery:
                            mBeaconParam.battery = Integer.parseInt(MokoUtils.bytesToHexString(value), 16) + "";
                            break;
                        case iBeaconUuid:
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
                        case major:
                            mBeaconParam.major = Integer.parseInt(MokoUtils.bytesToHexString(value), 16) + "";
                            break;
                        case minor:
                            mBeaconParam.minor = Integer.parseInt(MokoUtils.bytesToHexString(value), 16) + "";
                            break;
                        case measurePower:
                            mBeaconParam.measurePower = Integer.parseInt(MokoUtils.bytesToHexString(value), 16) + "";
                            break;
                        case transmission:
                            mBeaconParam.transmission = Integer.parseInt(MokoUtils.bytesToHexString(value), 16) + "";
                            break;
                        case broadcastingInterval:
                            mBeaconParam.broadcastingInterval = Integer.parseInt(MokoUtils.bytesToHexString(value), 16) + "";
                            break;
                        case serialID:
                            mBeaconParam.serialID = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
                            break;
                        case iBeaconMac:
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
                        case iBeaconName:
                            mBeaconParam.iBeaconName = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
                            break;
                        case connectionMode:
                            mBeaconParam.connectionMode = MokoUtils.bytesToHexString(value);
                            break;
                        case firmname:
                            mBeaconParam.beaconInfo.firmname = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
                            break;
                        case softVersion:
                            mBeaconParam.beaconInfo.softVersion = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
                            break;
                        case devicename:
                            mBeaconParam.beaconInfo.deviceName = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
                            break;
                        case iBeaconDate:
                            mBeaconParam.beaconInfo.iBeaconDate = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
                            break;
                        case hardwareVersion:
                            mBeaconParam.beaconInfo.hardwareVersion = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
                            break;
                        case firmwareVersion:
                            mBeaconParam.beaconInfo.firmwareVersion = MokoUtils.hex2String(MokoUtils.bytesToHexString(value));
                            break;
                        case writeAndNotify:
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
                        case changePassword:
                            if ("00".equals(MokoUtils.bytesToHexString(value))) {
                                mMokoService.mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        dismissLoadingProgressDialog();
                                        LogModule.i(mBeaconParam.toString());
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
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            if (animation != null) {
                                mMokoService.mHandler.removeMessages(0);
                                mMokoService.stopScanDevice();
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
        unbindService(mServiceConnection);
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
                    mMokoService.mHandler.removeMessages(0);
                    mMokoService.stopScanDevice();
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
        mMokoService.startScanDevice(this);
        mMokoService.mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mMokoService.stopScanDevice();
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
                mMokoService.mHandler.removeMessages(0);
                mMokoService.stopScanDevice();
            }
            final PasswordDialog dialog = new PasswordDialog(this);
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
                    mMokoService.connDevice(beaconInfo.mac);
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
                    dialog.showKeyboard();
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
}

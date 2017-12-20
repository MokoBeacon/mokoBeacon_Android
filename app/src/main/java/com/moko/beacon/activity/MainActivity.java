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
import android.os.Message;
import android.support.annotation.IdRes;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.moko.beacon.BeaconConstants;
import com.moko.beacon.R;
import com.moko.beacon.adapter.BeaconListAdapter;
import com.moko.beacon.base.BaseHandler;
import com.moko.beacon.dialog.PasswordDialog;
import com.moko.beacon.entity.BeaconDeviceInfo;
import com.moko.beacon.entity.BeaconParam;
import com.moko.beacon.service.BeaconService;
import com.moko.beacon.utils.ToastUtils;
import com.moko.beaconsupport.beacon.BeaconModule;
import com.moko.beaconsupport.callback.ScanDeviceCallback;
import com.moko.beaconsupport.entity.BeaconInfo;
import com.moko.beaconsupport.entity.OrderType;
import com.moko.beaconsupport.log.LogModule;
import com.moko.beaconsupport.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
public class MainActivity extends Activity implements RadioGroup.OnCheckedChangeListener, ScanDeviceCallback, AdapterView.OnItemClickListener {

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

    private BeaconListAdapter mAdapter;
    private ArrayList<BeaconInfo> mBeaconInfos;
    private int mSortType;
    private String mFilterText;
    private BeaconService mBeaconService;
    private boolean mIsChangePasswordSuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mAdapter = new BeaconListAdapter(this);
        mBeaconInfos = new ArrayList<>();
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
        bindService(new Intent(this, BeaconService.class), mServiceConnection, BIND_AUTO_CREATE);
    }

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
            filter.setPriority(100);
            registerReceiver(mReceiver, filter);
            if (!BeaconModule.getInstance().isBluetoothOpen()) {
                // 蓝牙未打开，开启蓝牙
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, BeaconConstants.REQUEST_CODE_ENABLE_BT);
            } else {
                mBeaconService.startScanDevice(MainActivity.this);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private boolean mIsScaning;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (BeaconConstants.ACTION_CONNECT_SUCCESS.equals(action)) {
                    mBeaconParam = new BeaconParam();
                    BeaconDeviceInfo beaconInfo = new BeaconDeviceInfo();
                    mBeaconParam.beaconInfo = beaconInfo;
                    // 读取全部可读数据
                    mBeaconService.mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mBeaconParam.password = mPassword;
                            mBeaconService.getReadableData(mPassword);
                        }
                    }, 1000);
                }
                if (BeaconConstants.ACTION_CONNECT_DISCONNECTED.equals(action)) {
                    dismissLoadingProgressDialog();
                    ToastUtils.showToast(MainActivity.this, "connect failed");
                }
                if (BeaconConstants.ACTION_RESPONSE_TIMEOUT.equals(action)) {
                    OrderType orderType = (OrderType) intent.getSerializableExtra(BeaconConstants.EXTRA_KEY_RESPONSE_ORDER_TYPE);
                    switch (orderType) {
                        case changePassword:
                            // 修改密码超时
                            break;
                    }
                }
                if (BeaconConstants.ACTION_RESPONSE_FINISH.equals(action)) {
                    mBeaconService.mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dismissLoadingProgressDialog();
                            if (mIsChangePasswordSuccess) {
                                mBeaconService.stopScanDevice();
                                mIsScaning = false;
                                LogModule.i(mBeaconParam.toString());
                                Intent deviceInfoIntent = new Intent(MainActivity.this, DeviceInfoActivity.class);
                                deviceInfoIntent.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_PARAM, mBeaconParam);
                                startActivityForResult(deviceInfoIntent, BeaconConstants.REQUEST_CODE_DEVICE_INFO);
                            } else {
                                ToastUtils.showToast(MainActivity.this, "password error");
                            }
                        }
                    }, 1000);
                }
                if (BeaconConstants.ACTION_RESPONSE_SUCCESS.equals(action)) {
                    OrderType orderType = (OrderType) intent.getSerializableExtra(BeaconConstants.EXTRA_KEY_RESPONSE_ORDER_TYPE);
                    byte[] value = intent.getByteArrayExtra(BeaconConstants.EXTRA_KEY_RESPONSE_VALUE);
                    switch (orderType) {
                        case battery:
                            mBeaconParam.battery = Integer.parseInt(Utils.bytesToHexString(value), 16);
                            break;
                        case iBeaconUuid:
                            String hexString = Utils.bytesToHexString(value).toUpperCase();
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
                            mBeaconParam.major = Integer.parseInt(Utils.bytesToHexString(value), 16);
                            break;
                        case minor:
                            mBeaconParam.minor = Integer.parseInt(Utils.bytesToHexString(value), 16);
                            break;
                        case measurePower:
                            mBeaconParam.measurePower = Integer.parseInt(Utils.bytesToHexString(value), 16);
                            break;
                        case transmission:
                            mBeaconParam.transmission = Integer.parseInt(Utils.bytesToHexString(value), 16);
                            break;
                        case broadcastingInterval:
                            mBeaconParam.broadcastingInterval = Integer.parseInt(Utils.bytesToHexString(value), 16);
                            break;
                        case serialID:
                            mBeaconParam.serialID = Utils.hex2String(Utils.bytesToHexString(value));
                            break;
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
                                mBeaconParam.iBeaconMAC = mac;
                                mBeaconParam.beaconInfo.iBeaconMac = mac;
                            }
                            break;
                        case iBeaconName:
                            mBeaconParam.iBeaconName = Utils.hex2String(Utils.bytesToHexString(value));
                            break;
                        case connectionMode:
                            mBeaconParam.connectionMode = Utils.bytesToHexString(value);
                            break;
                        case firmname:
                            mBeaconParam.beaconInfo.firmname = Utils.hex2String(Utils.bytesToHexString(value));
                            break;
                        case devicename:
                            mBeaconParam.beaconInfo.deviceName = Utils.hex2String(Utils.bytesToHexString(value));
                            break;
                        case iBeaconDate:
                            mBeaconParam.beaconInfo.iBeaconDate = Utils.hex2String(Utils.bytesToHexString(value));
                            break;
                        case hardwareVersion:
                            mBeaconParam.beaconInfo.hardwareVersion = Utils.hex2String(Utils.bytesToHexString(value));
                            break;
                        case firmwareVersion:
                            mBeaconParam.beaconInfo.firmwareVersion = Utils.hex2String(Utils.bytesToHexString(value));
                            break;
                        case runtimeAndChipModel:
                            if ("0004".equals(Utils.bytesToHexString(Arrays.copyOfRange(value, 0, 2)))) {
                                byte[] runtimeBytes = Arrays.copyOfRange(value, 2, value.length);
                                int runtime = Integer.parseInt(Utils.bytesToHexString(runtimeBytes), 16);
                                int runtimeDays = runtime / (60 * 60 * 24);
                                int runtimeHours = (runtime % (60 * 60 * 24)) / (60 * 60);
                                int runtimeMinutes = (runtime % (60 * 60)) / (60);
                                int runtimeSeconds = (runtime % (60)) / 1000;
                                mBeaconParam.beaconInfo.runtime = String.format("%dD%dh%dm%ds", runtimeDays, runtimeHours, runtimeMinutes, runtimeSeconds);
                            }
                            if ("000c".equals(Utils.bytesToHexString(Arrays.copyOfRange(value, 0, 2)).toLowerCase())) {
                                byte[] chipModelBytes = Arrays.copyOfRange(value, 2, value.length);
                                mBeaconParam.beaconInfo.chipModel = Utils.hex2String(Utils.bytesToHexString(chipModelBytes));
                            }
                            break;
                        case systemMark:
                            mBeaconParam.beaconInfo.systemMark = Utils.bytesToHexString(value);
                        case IEEEInfo:
                            mBeaconParam.beaconInfo.IEEEInfo = Utils.bytesToHexString(value);
                            break;
                        case changePassword:
                            if ("00".equals(Utils.bytesToHexString(value))) {
                                mIsChangePasswordSuccess = true;
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
                case BeaconConstants.REQUEST_CODE_ENABLE_BT:
                    // 打开蓝牙
                    if (!mIsScaning) {
                        mBeaconService.startScanDevice(MainActivity.this);
                        mIsScaning = true;
                    }
                    break;

            }
        } else {
            switch (requestCode) {
                case BeaconConstants.REQUEST_CODE_ENABLE_BT:
                    // 未打开蓝牙
                    break;
                case BeaconConstants.REQUEST_CODE_DEVICE_INFO:
                    mIsChangePasswordSuccess = false;
                    if (!mIsScaning) {
                        mBeaconService.startScanDevice(MainActivity.this);
                        mIsScaning = true;
                    }
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        mBeaconService.stopScanDevice();
        mIsScaning = false;
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

    @OnClick({R.id.iv_about})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }
    }

    @Override
    public void onStartScan() {
        showProgressDialog();
    }

    private void showProgressDialog() {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage(getString(R.string.main_scan));
        if (!isFinishing() && dialog != null && !dialog.isShowing()) {
            dialog.show();
            mBeaconService.mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dialog.dismiss();
                }
            }, 2000);
        }
    }

    @Override
    public void onScanDevice(ArrayList<BeaconInfo> beaconInfos) {
        mBeaconInfos.clear();
        // 名称过滤
        if (TextUtils.isEmpty(mFilterText)) {
            mBeaconInfos.addAll(beaconInfos);
        } else {
            for (BeaconInfo beaconInfo : beaconInfos) {
                if (beaconInfo.name.toLowerCase().contains(mFilterText.toLowerCase())) {
                    mBeaconInfos.add(beaconInfo);
                }
            }
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
    }

    @Override
    public void onStopScan() {

    }

    private BeaconParam mBeaconParam;
    private String mPassword;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final BeaconInfo beaconInfo = (BeaconInfo) parent.getItemAtPosition(position);
        LogModule.i(beaconInfo.toString());
        if (!isFinishing()) {
            final PasswordDialog dialog = new PasswordDialog(this);
            dialog.setOnPasswordClicked(new PasswordDialog.PasswordClickListener() {
                @Override
                public void onEnsureClicked(String password) {
                    LogModule.i(password);
                    mPassword = password;
                    mBeaconService.connDevice(beaconInfo.mac);
                    showLoadingProgressDialog();
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


    private class CustomHandler extends BaseHandler<MainActivity> {

        public CustomHandler(MainActivity activity) {
            super(activity);
        }

        @Override
        protected void handleMessage(MainActivity activity, Message msg) {
        }
    }
}

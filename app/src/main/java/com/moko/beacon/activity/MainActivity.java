package com.moko.beacon.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.IdRes;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.moko.beacon.R;
import com.moko.beacon.adapter.DeviceListAdapter;
import com.moko.beacon.base.BaseHandler;
import com.moko.beacon.dialog.PasswordDialog;
import com.moko.beacon.entity.DeviceInfo;
import com.moko.beacon.utils.Utils;
import com.moko.beaconsupport.beacon.BeaconModule;
import com.moko.beaconsupport.callback.ScanDeviceCallback;
import com.moko.beaconsupport.log.LogModule;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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

    @Bind(R.id.iv_about)
    ImageView ivAbout;
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

    private DeviceListAdapter mAdapter;
    private ArrayList<DeviceInfo> mDeviceInfos;
    private HashMap<String, DeviceInfo> mDeviceMap;
    private int mSortType;
    private String mFilterText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mHandler = new CustomHandler(this);
        mAdapter = new DeviceListAdapter(this);
        mDeviceInfos = new ArrayList<>();
        mDeviceMap = new HashMap<>();
        mAdapter.setItems(mDeviceInfos);
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
        BeaconModule.getInstance().startScanDevice(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BeaconModule.getInstance().stopScanDevice();
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
        dialog.setMessage("扫描中...");
        if (!isFinishing() && dialog != null && !dialog.isShowing()) {
            dialog.show();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dialog.dismiss();
                }
            }, 2000);
        }
    }

    @Override
    public void onScanDevice(BluetoothDevice device, int rssi, byte[] scanRecord) {
        int startByte = 2;
        boolean patternFound = false;
        // 0215 00ff
        while (startByte <= 5) {
            if (((int) scanRecord[startByte + 2] & 0xff) == 0x02
                    && ((int) scanRecord[startByte + 3] & 0xff) == 0x15
                    && ((int) scanRecord[startByte + 30] & 0xff) == 0x00
                    && ((int) scanRecord[startByte + 31] & 0xff) == 0xff) {
                // yes!  This is an iBeacon
                patternFound = true;
                break;
            }
            startByte++;
        }
        if (patternFound == false) {
            // This is not an iBeacon
            return;
        }
        // log
        String log = Utils.bytesToHexString(scanRecord);
        // uuid
        byte[] proximityUuidBytes = new byte[16];
        System.arraycopy(scanRecord, startByte + 4, proximityUuidBytes, 0, 16);
        String hexString = Utils.bytesToHexString(proximityUuidBytes);
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

        int major = (scanRecord[startByte + 20] & 0xff) * 0x100 + (scanRecord[startByte + 21] & 0xff);
        int minor = (scanRecord[startByte + 22] & 0xff) * 0x100 + (scanRecord[startByte + 23] & 0xff);
        int txPower = 0 - (int) scanRecord[startByte + 27] & 0xff;
        int battery = (int) scanRecord[startByte + 32] & 0xff;
        // distance acc
        int acc = (int) scanRecord[startByte + 37] & 0xff;
        int version = (int) scanRecord[startByte + 38] & 0xff;
        String mac = device.getAddress();
        double distance = Utils.getDistance(rssi, acc);
        String distanceDesc = "unknown";
        if (distance <= 0.1) {
            distanceDesc = "immediate";
        } else if (distance > 0.1 && distance <= 1.0) {
            distanceDesc = "near";
        } else if (distance > 1.0) {
            distanceDesc = "far";
        }
        String distanceStr = new DecimalFormat("#0.00").format(distance);
        if (!mDeviceMap.isEmpty() && mDeviceMap.containsKey(mac)) {
            DeviceInfo deviceInfo = mDeviceMap.get(mac);
            deviceInfo.name = device.getName();
            deviceInfo.rssi = rssi;
            deviceInfo.distance = distanceStr;
            deviceInfo.distanceDesc = distanceDesc;
            deviceInfo.major = major;
            deviceInfo.minor = minor;
            deviceInfo.txPower = txPower;
            deviceInfo.uuid = uuid;
            deviceInfo.batteryPower = battery;
            deviceInfo.version = version;
            deviceInfo.scanRecord = log;
        } else {
            DeviceInfo deviceInfo = new DeviceInfo();
            deviceInfo.name = device.getName();
            deviceInfo.rssi = rssi;
            deviceInfo.distance = distanceStr;
            deviceInfo.distanceDesc = distanceDesc;
            deviceInfo.major = major;
            deviceInfo.minor = minor;
            deviceInfo.txPower = txPower;
            deviceInfo.uuid = uuid;
            deviceInfo.batteryPower = battery;
            deviceInfo.version = version;
            deviceInfo.scanRecord = log;
            deviceInfo.mac = mac;
            mDeviceMap.put(deviceInfo.mac, deviceInfo);
        }
        mDeviceInfos.clear();
        // 名称过滤
        if (TextUtils.isEmpty(mFilterText)) {
            mDeviceInfos.addAll(mDeviceMap.values());
        } else {
            for (DeviceInfo deviceInfo : mDeviceMap.values()) {
                if (deviceInfo.name.toLowerCase().contains(mFilterText.toLowerCase())) {
                    mDeviceInfos.add(deviceInfo);
                }
            }
        }
        // 排序
        switch (mSortType) {
            case SORT_TYPE_RSSI:
                if (!mDeviceInfos.isEmpty()) {
                    Collections.sort(mDeviceInfos, new Comparator<DeviceInfo>() {
                        @Override
                        public int compare(DeviceInfo lhs, DeviceInfo rhs) {
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
                if (!mDeviceInfos.isEmpty()) {
                    Collections.sort(mDeviceInfos, new Comparator<DeviceInfo>() {
                        @Override
                        public int compare(DeviceInfo lhs, DeviceInfo rhs) {
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
                if (!mDeviceInfos.isEmpty()) {
                    Collections.sort(mDeviceInfos, new Comparator<DeviceInfo>() {
                        @Override
                        public int compare(DeviceInfo lhs, DeviceInfo rhs) {
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

    private CustomHandler mHandler;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DeviceInfo deviceInfo = (DeviceInfo) parent.getItemAtPosition(position);
        LogModule.i(deviceInfo.toString());
        if (!isFinishing()) {
            final PasswordDialog dialog = new PasswordDialog(this);
            dialog.setOnPasswordClicked(new PasswordDialog.PasswordClickListener() {
                @Override
                public void onEnsureClicked(String password) {
                    LogModule.i(password);
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

    private class CustomHandler extends BaseHandler<MainActivity> {

        public CustomHandler(MainActivity activity) {
            super(activity);
        }

        @Override
        protected void handleMessage(MainActivity activity, Message msg) {
        }
    }
}

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
import com.moko.beacon.service.BeaconService;
import com.moko.beaconsupport.beacon.BeaconModule;
import com.moko.beaconsupport.callback.ScanDeviceCallback;
import com.moko.beaconsupport.entity.BeaconInfo;
import com.moko.beaconsupport.log.LogModule;

import java.util.ArrayList;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mHandler = new CustomHandler(this);
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

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (BeaconConstants.ACTION_CONNECT_SUCCESS.equals(action)) {
                    mBeaconService.stopScanDevice();
                    Intent deviceInfoIntent = new Intent(MainActivity.this, DeviceInfoActivity.class);
                    deviceInfoIntent.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_INFO, mBeaconInfo);
                    startActivityForResult(deviceInfoIntent, BeaconConstants.REQUEST_CODE_DEVICE_INFO);
                }
                if (BeaconConstants.ACTION_CONNECT_DISCONNECTED.equals(action)) {

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
                    mBeaconService.startScanDevice(MainActivity.this);
                    break;

            }
        } else {
            switch (requestCode) {
                case BeaconConstants.REQUEST_CODE_ENABLE_BT:
                    // 未打开蓝牙
                    break;
                case BeaconConstants.REQUEST_CODE_DEVICE_INFO:
                    mBeaconService.startScanDevice(MainActivity.this);
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        mBeaconService.stopScanDevice();
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

    private CustomHandler mHandler;
    private BeaconInfo mBeaconInfo;

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
                    mBeaconService.connDevice(beaconInfo.mac);
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

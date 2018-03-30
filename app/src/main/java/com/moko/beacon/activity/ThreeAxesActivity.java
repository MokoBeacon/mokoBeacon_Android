package com.moko.beacon.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ScrollView;
import android.widget.TextView;

import com.moko.beacon.BeaconConstants;
import com.moko.beacon.R;
import com.moko.beacon.service.BeaconService;
import com.moko.beacon.utils.ToastUtils;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.entity.OrderType;
import com.moko.support.log.LogModule;
import com.moko.support.utils.MokoUtils;
import com.moko.support.utils.MokoUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @Date 2018/1/10
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beacon.activity.ThreeAxesActivity
 */
public class ThreeAxesActivity extends BaseActivity {

    @Bind(R.id.tv_device_three_axis)
    TextView tvDeviceThreeAxis;
    @Bind(R.id.tv_stop)
    TextView tvStop;
    @Bind(R.id.scroll_view)
    ScrollView scrollView;
    private BeaconService mBeaconService;
    private StringBuilder builder;
    private SimpleDateFormat simpleDateFormat;
    private boolean isNotifyOn;
    private boolean isBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_three_axis);
        ButterKnife.bind(this);
        bindService(new Intent(this, BeaconService.class), mServiceConnection, BIND_AUTO_CREATE);
        simpleDateFormat = new SimpleDateFormat(BeaconConstants.PATTERN_HH_MM_SS);
        builder = new StringBuilder();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        unbindService(mServiceConnection);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            abortBroadcast();
            if (intent != null) {
                String action = intent.getAction();
                if (MokoConstants.ACTION_CONNECT_DISCONNECTED.equals(action)) {
                    dismissLoadingProgressDialog();
                    ToastUtils.showToast(ThreeAxesActivity.this, getString(R.string.alert_diconnected));
                    ThreeAxesActivity.this.setResult(BeaconConstants.RESULT_CONN_DISCONNECTED);
                    finish();
                }
                if (MokoConstants.ACTION_RESPONSE_TIMEOUT.equals(action)) {
                    dismissLoadingProgressDialog();
                    OrderType orderType = (OrderType) intent.getSerializableExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TYPE);
                    switch (orderType) {
                        case writeAndNotify:
                            ToastUtils.showToast(ThreeAxesActivity.this, getString(R.string.read_data_failed));
                            finish();
                            break;
                    }
                }
                if (MokoConstants.ACTION_RESPONSE_SUCCESS.equals(action)) {
                    dismissLoadingProgressDialog();
                    OrderType orderType = (OrderType) intent.getSerializableExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TYPE);
                    switch (orderType) {
                        case writeAndNotify:
                            if (!isNotifyOn) {
                                if (isBack) {
                                    finish();
                                } else {
                                    tvStop.setText("Start");
                                    LogModule.i("三轴加速度计关闭");
                                }
                            } else {
                                tvStop.setText("Stop");
                                LogModule.i("三轴加速度计打开");
                            }
                            break;
                    }
                }
                if (MokoConstants.ACTION_RESPONSE_NOTIFY.equals(action)) {
                    OrderType orderType = (OrderType) intent.getSerializableExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TYPE);
                    byte[] value = intent.getByteArrayExtra(MokoConstants.EXTRA_KEY_RESPONSE_VALUE);
                    switch (orderType) {
                        case writeAndNotify:
                            builder.append(simpleDateFormat.format(Calendar.getInstance().getTime()));
                            String threeAxisStr = MokoUtils.bytesToHexString(value);
                            if (threeAxisStr.length() >= 20) {
                                builder.append(String.format("----<X：%s；Y：%s；Z：%s>", threeAxisStr.substring(8, 12), threeAxisStr.substring(12, 16), threeAxisStr.substring(16, 20)));
                                builder.append("\n");
                                tvDeviceThreeAxis.setText(builder.toString());
                                scrollView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                                    }
                                });
                            }
                            break;
                    }
                }
            }
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBeaconService = ((BeaconService.LocalBinder) service).getService();
            // 注册广播接收器
            IntentFilter filter = new IntentFilter();
            filter.addAction(MokoConstants.ACTION_CONNECT_SUCCESS);
            filter.addAction(MokoConstants.ACTION_CONNECT_DISCONNECTED);
            filter.addAction(MokoConstants.ACTION_RESPONSE_SUCCESS);
            filter.addAction(MokoConstants.ACTION_RESPONSE_TIMEOUT);
            filter.addAction(MokoConstants.ACTION_RESPONSE_FINISH);
            filter.addAction(MokoConstants.ACTION_RESPONSE_NOTIFY);
            filter.setPriority(300);
            registerReceiver(mReceiver, filter);
            if (!MokoSupport.getInstance().isBluetoothOpen()) {
                ToastUtils.showToast(ThreeAxesActivity.this, "bluetooth is closed,please open");
                return;
            }
            showLoadingProgressDialog("");
            mBeaconService.sendOrder(mBeaconService.setThreeAxes(true));
            isNotifyOn = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @OnClick({R.id.tv_back, R.id.tv_stop})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                back();
                break;
            case R.id.tv_stop:
                if (!MokoSupport.getInstance().isBluetoothOpen()) {
                    ToastUtils.showToast(this, "bluetooth is closed,please open");
                    return;
                }
                if (isNotifyOn) {
                    showLoadingProgressDialog("");
                    mBeaconService.sendOrder(mBeaconService.setThreeAxes(false));
                    isNotifyOn = false;
                } else {
                    showLoadingProgressDialog("");
                    mBeaconService.sendOrder(mBeaconService.setThreeAxes(true));
                    isNotifyOn = true;
                }
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
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            ToastUtils.showToast(this, "bluetooth is closed,please open");
            finish();
            return;
        }
        if (isNotifyOn) {
            showLoadingProgressDialog("");
            mBeaconService.sendOrder(mBeaconService.setThreeAxes(false));
            isNotifyOn = false;
            isBack = true;
        } else {
            finish();
        }
    }

    private ProgressDialog mLoadingDialog;

    private void showLoadingProgressDialog(String tips) {
        mLoadingDialog = new ProgressDialog(ThreeAxesActivity.this);
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
}

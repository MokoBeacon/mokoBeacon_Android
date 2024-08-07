package com.moko.beacon.activity;

import android.os.Bundle;
import android.widget.ScrollView;

import com.elvishew.xlog.XLog;
import com.moko.beacon.BeaconConstants;
import com.moko.beacon.R;
import com.moko.beacon.databinding.ActivityThreeAxisBinding;
import com.moko.beacon.dialog.LoadingDialog;
import com.moko.beacon.utils.ToastUtils;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.MokoSupport;
import com.moko.support.OrderTaskAssembler;
import com.moko.support.entity.OrderCHAR;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import androidx.annotation.Nullable;

public class ThreeAxesActivity extends BaseActivity {


    private ActivityThreeAxisBinding mBind;
    private StringBuilder builder;
    private SimpleDateFormat simpleDateFormat;
    private boolean isNotifyOn;
    private boolean isBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityThreeAxisBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        simpleDateFormat = new SimpleDateFormat(BeaconConstants.PATTERN_HH_MM_SS);
        builder = new StringBuilder();
        EventBus.getDefault().register(this);
        showLoadingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setThreeAxes(true));
        isNotifyOn = true;
        mBind.tvBack.setOnClickListener(v -> back());
        mBind.tvStop.setOnClickListener(v -> {
            if (!MokoSupport.getInstance().isBluetoothOpen()) {
                ToastUtils.showToast(this, "bluetooth is closed,please open");
                return;
            }
            if (isNotifyOn) {
                showLoadingProgressDialog();
                MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setThreeAxes(false));
                isNotifyOn = false;
            } else {
                showLoadingProgressDialog();
                MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setThreeAxes(true));
                isNotifyOn = true;
            }
        });
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
                dismissLoadingProgressDialog();
                ToastUtils.showToast(ThreeAxesActivity.this, getString(R.string.alert_diconnected));
                ThreeAxesActivity.this.setResult(BeaconConstants.RESULT_CONN_DISCONNECTED);
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
                dismissLoadingProgressDialog();
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderCHAR) {
                    case CHAR_PARAMS:
                        ToastUtils.showToast(ThreeAxesActivity.this, getString(R.string.read_data_failed));
                        if (EventBus.getDefault().isRegistered(this))
                            EventBus.getDefault().unregister(this);
                        finish();
                        break;
                }
            }
            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                dismissLoadingProgressDialog();
            }
            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                dismissLoadingProgressDialog();
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderCHAR) {
                    case CHAR_PARAMS:
                        if (!isNotifyOn) {
                            if (isBack) {
                                finish();
                            } else {
                                mBind.tvStop.setText("Start");
                                XLog.i("三轴加速度计关闭");
                            }
                        } else {
                            mBind.tvStop.setText("Stop");
                            XLog.i("三轴加速度计打开");
                        }
                        break;
                }
            }
            if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderCHAR) {
                    case CHAR_PARAMS:
                        builder.append(simpleDateFormat.format(Calendar.getInstance().getTime()));
                        String threeAxisStr = MokoUtils.bytesToHexString(value);
                        if (threeAxisStr.length() >= 20) {
                            builder.append(String.format("----<X：%s；Y：%s；Z：%s>", threeAxisStr.substring(8, 12), threeAxisStr.substring(12, 16), threeAxisStr.substring(16, 20)));
                            builder.append("\n");
                            mBind.tvDeviceThreeAxis.setText(builder.toString());
                            mBind.scrollView.post(new Runnable() {
                                @Override
                                public void run() {
                                    mBind.scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                                }
                            });
                        }
                        break;
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        back();
    }

    private void back() {
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            if (EventBus.getDefault().isRegistered(this))
                EventBus.getDefault().unregister(this);
            ToastUtils.showToast(this, "bluetooth is closed,please open");
            finish();
            return;
        }
        if (isNotifyOn) {
            showLoadingProgressDialog();
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setThreeAxes(false));
            isNotifyOn = false;
            isBack = true;
        } else {
            if (EventBus.getDefault().isRegistered(this))
                EventBus.getDefault().unregister(this);
            finish();
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

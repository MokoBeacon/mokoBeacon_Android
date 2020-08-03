package com.moko.beacon.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ScrollView;
import android.widget.TextView;

import com.moko.beacon.BeaconConstants;
import com.moko.beacon.R;
import com.moko.beacon.utils.ToastUtils;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.OrderTaskAssembler;
import com.moko.support.entity.OrderType;
import com.moko.support.event.ConnectStatusEvent;
import com.moko.support.event.OrderTaskResponseEvent;
import com.moko.support.log.LogModule;
import com.moko.support.task.OrderTaskResponse;
import com.moko.support.utils.MokoUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
    private StringBuilder builder;
    private SimpleDateFormat simpleDateFormat;
    private boolean isNotifyOn;
    private boolean isBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_three_axis);
        ButterKnife.bind(this);
        simpleDateFormat = new SimpleDateFormat(BeaconConstants.PATTERN_HH_MM_SS);
        builder = new StringBuilder();
        EventBus.getDefault().register(this);
        showLoadingProgressDialog("");
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setThreeAxes(true));
        isNotifyOn = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_CONN_STATUS_DISCONNECTED.equals(action)) {
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
                OrderType orderType = response.orderType;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderType) {
                    case PARAMS_CONFIG:
                        ToastUtils.showToast(ThreeAxesActivity.this, getString(R.string.read_data_failed));
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
                OrderType orderType = response.orderType;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderType) {
                    case PARAMS_CONFIG:
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
            if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderType orderType = response.orderType;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderType) {
                    case PARAMS_CONFIG:
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
        });
    }


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
                    MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setThreeAxes(false));
                    isNotifyOn = false;
                } else {
                    showLoadingProgressDialog("");
                    MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setThreeAxes(true));
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
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setThreeAxes(false));
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

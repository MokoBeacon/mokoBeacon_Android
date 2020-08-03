package com.moko.beacon.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
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
import com.moko.support.task.OrderTaskResponse;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @Date 2017/12/18 0018
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beacon.activity.SetBroadcastIntervalActivity
 */
public class SetBroadcastIntervalActivity extends BaseActivity {

    @Bind(R.id.tv_broadcast_interval_1)
    TextView tvBroadcastInterval1;
    @Bind(R.id.tv_broadcast_interval_2)
    TextView tvBroadcastInterval2;
    @Bind(R.id.tv_broadcast_interval_3)
    TextView tvBroadcastInterval3;
    @Bind(R.id.tv_broadcast_interval_4)
    TextView tvBroadcastInterval4;
    @Bind(R.id.tv_broadcast_interval_5)
    TextView tvBroadcastInterval5;
    @Bind(R.id.tv_broadcast_interval_6)
    TextView tvBroadcastInterval6;
    @Bind(R.id.tv_broadcast_interval_7)
    TextView tvBroadcastInterval7;
    @Bind(R.id.tv_broadcast_interval_8)
    TextView tvBroadcastInterval8;
    @Bind(R.id.tv_broadcast_interval_9)
    TextView tvBroadcastInterval9;
    @Bind(R.id.tv_broadcast_interval_10)
    TextView tvBroadcastInterval10;
    @Bind(R.id.et_broadcast_interval)
    EditText etBroadcastInterval;
    private ArrayList<View> mViews;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast_interval);
        ButterKnife.bind(this);
        int broadcastInterval = getIntent().getIntExtra(BeaconConstants.EXTRA_KEY_DEVICE_BROADCASTINTERVAL, 0) - 1;
        mViews = new ArrayList<>();
        mViews.add(tvBroadcastInterval1);
        mViews.add(tvBroadcastInterval2);
        mViews.add(tvBroadcastInterval3);
        mViews.add(tvBroadcastInterval4);
        mViews.add(tvBroadcastInterval5);
        mViews.add(tvBroadcastInterval6);
        mViews.add(tvBroadcastInterval7);
        mViews.add(tvBroadcastInterval8);
        mViews.add(tvBroadcastInterval9);
        mViews.add(tvBroadcastInterval10);
        if (broadcastInterval > 9) {
            etBroadcastInterval.setText((broadcastInterval + 1) + "");
            etBroadcastInterval.setSelection(String.valueOf(broadcastInterval + 1).length());
        } else {
            setViewSeleceted(broadcastInterval);
        }
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_CONN_STATUS_DISCONNECTED.equals(action)) {
                ToastUtils.showToast(SetBroadcastIntervalActivity.this, getString(R.string.alert_diconnected));
                SetBroadcastIntervalActivity.this.setResult(BeaconConstants.RESULT_CONN_DISCONNECTED);
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
                OrderTaskResponse response = event.getResponse();
                OrderType orderType = response.orderType;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderType) {
                    case ADV_INTERVAL:
                        // 修改broadcastingInterval失败
                        ToastUtils.showToast(SetBroadcastIntervalActivity.this, getString(R.string.read_data_failed));
                        finish();
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
                    case ADV_INTERVAL:
                        // 修改broadcastingInterval成功
                        Intent i = new Intent();
                        i.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_BROADCASTINTERVAL, Integer.parseInt(etBroadcastInterval.getText().toString()));
                        SetBroadcastIntervalActivity.this.setResult(RESULT_OK, i);
                        finish();
                        break;
                }
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @OnClick({R.id.tv_back, R.id.iv_save, R.id.tv_broadcast_interval_1, R.id.tv_broadcast_interval_2,
            R.id.tv_broadcast_interval_3, R.id.tv_broadcast_interval_4, R.id.tv_broadcast_interval_5,
            R.id.tv_broadcast_interval_6, R.id.tv_broadcast_interval_7, R.id.tv_broadcast_interval_8,
            R.id.tv_broadcast_interval_9, R.id.tv_broadcast_interval_10})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                finish();
                break;
            case R.id.iv_save:
                if (!MokoSupport.getInstance().isBluetoothOpen()) {
                    ToastUtils.showToast(this, "bluetooth is closed,please open");
                    return;
                }
                String broadcastIntervalStr = etBroadcastInterval.getText().toString();
                if (TextUtils.isEmpty(broadcastIntervalStr)) {
                    ToastUtils.showToast(this, getString(R.string.alert_data_cannot_null));
                    return;
                }
                int broadcastIntervalValue = Integer.parseInt(broadcastIntervalStr);
                if (broadcastIntervalValue > 100 || broadcastIntervalValue <= 0) {
                    ToastUtils.showToast(this, getString(R.string.alert_broadcast_interval_range));
                    return;
                }
                MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setAdvInterval(broadcastIntervalValue));
                break;
            case R.id.tv_broadcast_interval_1:
            case R.id.tv_broadcast_interval_2:
            case R.id.tv_broadcast_interval_3:
            case R.id.tv_broadcast_interval_4:
            case R.id.tv_broadcast_interval_5:
            case R.id.tv_broadcast_interval_6:
            case R.id.tv_broadcast_interval_7:
            case R.id.tv_broadcast_interval_8:
            case R.id.tv_broadcast_interval_9:
            case R.id.tv_broadcast_interval_10:
                int broadcastInterval = Integer.valueOf((String) view.getTag()) - 1;
                setViewSeleceted(broadcastInterval);
                break;

        }
    }

    private void setViewSeleceted(int broadcastInterval) {
        for (View view : mViews) {
            view.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_radius_big_white_bg));
            ((TextView) view).setTextColor(ContextCompat.getColor(this, R.color.blue_5691fc));
        }
        View selected = mViews.get(broadcastInterval);
        selected.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_radius_big_blue_bg));
        ((TextView) selected).setTextColor(ContextCompat.getColor(this, R.color.white_ffffff));
        etBroadcastInterval.setText((broadcastInterval + 1) + "");
        etBroadcastInterval.setSelection(String.valueOf(broadcastInterval + 1).length());
    }
}

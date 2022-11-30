package com.moko.beacon.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.moko.beacon.BeaconConstants;
import com.moko.beacon.R;
import com.moko.beacon.databinding.ActivityBroadcastIntervalBinding;
import com.moko.beacon.utils.ToastUtils;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.support.MokoSupport;
import com.moko.support.OrderTaskAssembler;
import com.moko.support.entity.OrderCHAR;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;


public class SetBroadcastIntervalActivity extends BaseActivity {


    private ActivityBroadcastIntervalBinding mBind;
    private ArrayList<View> mViews;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityBroadcastIntervalBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        int broadcastInterval = getIntent().getIntExtra(BeaconConstants.EXTRA_KEY_DEVICE_BROADCASTINTERVAL, 0) - 1;
        mViews = new ArrayList<>();
        mViews.add(mBind.tvBroadcastInterval1);
        mViews.add(mBind.tvBroadcastInterval2);
        mViews.add(mBind.tvBroadcastInterval3);
        mViews.add(mBind.tvBroadcastInterval4);
        mViews.add(mBind.tvBroadcastInterval5);
        mViews.add(mBind.tvBroadcastInterval6);
        mViews.add(mBind.tvBroadcastInterval7);
        mViews.add(mBind.tvBroadcastInterval8);
        mViews.add(mBind.tvBroadcastInterval9);
        mViews.add(mBind.tvBroadcastInterval10);
        if (broadcastInterval > 9) {
            mBind.etBroadcastInterval.setText((broadcastInterval + 1) + "");
            mBind.etBroadcastInterval.setSelection(String.valueOf(broadcastInterval + 1).length());
        } else {
            setViewSeleceted(broadcastInterval);
        }
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
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
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderCHAR) {
                    case CHAR_ADV_INTERVAL:
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
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderCHAR) {
                    case CHAR_ADV_INTERVAL:
                        // 修改broadcastingInterval成功
                        Intent i = new Intent();
                        i.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_BROADCASTINTERVAL, Integer.parseInt(mBind.etBroadcastInterval.getText().toString()));
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


    public void onAdvInterval(View view) {
        if (isWindowLocked()) return;
        int broadcastInterval = Integer.valueOf((String) view.getTag()) - 1;
        setViewSeleceted(broadcastInterval);
    }

    private void setViewSeleceted(int broadcastInterval) {
        for (View view : mViews) {
            view.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_radius_big_white_bg));
            ((TextView) view).setTextColor(ContextCompat.getColor(this, R.color.blue_5691fc));
        }
        View selected = mViews.get(broadcastInterval);
        selected.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_radius_big_blue_bg));
        ((TextView) selected).setTextColor(ContextCompat.getColor(this, R.color.white_ffffff));
        mBind.etBroadcastInterval.setText((broadcastInterval + 1) + "");
        mBind.etBroadcastInterval.setSelection(String.valueOf(broadcastInterval + 1).length());
    }


    public void onBack(View view) {
        if (isWindowLocked()) return;
        finish();
    }

    public void onSave(View view) {
        if (isWindowLocked()) return;
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            ToastUtils.showToast(this, "bluetooth is closed,please open");
            return;
        }
        String broadcastIntervalStr = mBind.etBroadcastInterval.getText().toString();
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
    }
}

package com.moko.beacon.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.moko.beacon.BeaconConstants;
import com.moko.beacon.R;
import com.moko.beacon.databinding.ActivityMeasurePowerBinding;
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

import androidx.annotation.Nullable;

public class SetMeasurePowerActivity extends BaseActivity {
    private ActivityMeasurePowerBinding mBind;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityMeasurePowerBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        int measure_power = getIntent().getIntExtra(BeaconConstants.EXTRA_KEY_DEVICE_MEASURE_POWER, 0);
        mBind.etMeasurePower.setText(measure_power + "");
        mBind.etMeasurePower.setSelection(String.valueOf(measure_power).length());
        EventBus.getDefault().register(this);
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
                ToastUtils.showToast(SetMeasurePowerActivity.this, getString(R.string.alert_diconnected));
                SetMeasurePowerActivity.this.setResult(BeaconConstants.RESULT_CONN_DISCONNECTED);
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
                    case CHAR_MEASURE_POWER:
                        // 修改measure power失败
                        ToastUtils.showToast(SetMeasurePowerActivity.this, getString(R.string.read_data_failed));
                        if (EventBus.getDefault().isRegistered(this))
                            EventBus.getDefault().unregister(this);
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
                    case CHAR_MEASURE_POWER:
                        // 修改measure power成功
                        Intent i = new Intent();
                        i.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_MEASURE_POWER, Integer.valueOf(mBind.etMeasurePower.getText().toString()));
                        SetMeasurePowerActivity.this.setResult(RESULT_OK, i);
                        if (EventBus.getDefault().isRegistered(this))
                            EventBus.getDefault().unregister(this);
                        finish();
                        break;
                }
            }
        });
    }

    public void onBack(View view) {
        if (isWindowLocked()) return;
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        finish();
    }

    public void onSave(View view) {
        if (isWindowLocked()) return;
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            ToastUtils.showToast(this, "bluetooth is closed,please open");
            return;
        }
        if (TextUtils.isEmpty(mBind.etMeasurePower.getText().toString())) {
            ToastUtils.showToast(this, getString(R.string.alert_data_cannot_null));
            return;
        }
        int power = Integer.parseInt(mBind.etMeasurePower.getText().toString());
        if (power < 0 || power > 120) {
            ToastUtils.showToast(this, getString(R.string.alert_measure_power_range));
            return;
        }
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setMeasurePower(Integer.parseInt(mBind.etMeasurePower.getText().toString())));
    }
}

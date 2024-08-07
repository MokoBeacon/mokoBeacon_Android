package com.moko.beacon.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import com.moko.beacon.BeaconConstants;
import com.moko.beacon.R;
import com.moko.beacon.databinding.ActivityMajorBinding;
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

public class SetMajorActivity extends BaseActivity {

    private ActivityMajorBinding mBind;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityMajorBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        int major = getIntent().getIntExtra(BeaconConstants.EXTRA_KEY_DEVICE_MAJOR, 0);
        mBind.etMajor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString())) {
                    mBind.tvDecimalism.setText("");
                    mBind.tvHexadecimal.setText("");
                    return;
                }
                mBind.tvDecimalism.setText(s.toString());
                mBind.tvHexadecimal.setText(Integer.toHexString(Integer.parseInt(s.toString())));
            }
        });
        mBind.etMajor.setText(major + "");
        mBind.etMajor.setSelection(String.valueOf(major).length());
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
                ToastUtils.showToast(SetMajorActivity.this, getString(R.string.alert_diconnected));
                SetMajorActivity.this.setResult(BeaconConstants.RESULT_CONN_DISCONNECTED);
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
                    case CHAR_MAJOR:
                        // 修改major失败
                        ToastUtils.showToast(SetMajorActivity.this, getString(R.string.read_data_failed));
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
                    case CHAR_MAJOR:
                        // 修改major成功
                        Intent i = new Intent();
                        i.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_MAJOR, Integer.valueOf(mBind.etMajor.getText().toString()));
                        SetMajorActivity.this.setResult(RESULT_OK, i);
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
        if (TextUtils.isEmpty(mBind.etMajor.getText().toString())) {
            ToastUtils.showToast(this, getString(R.string.alert_data_cannot_null));
            return;
        }
        if (Integer.parseInt(mBind.etMajor.getText().toString()) > 65535 || Integer.parseInt(mBind.etMajor.getText().toString()) < 0) {
            ToastUtils.showToast(this, getString(R.string.alert_minor_range));
            return;
        }
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setMajor(Integer.parseInt(mBind.etMajor.getText().toString())));
    }
}

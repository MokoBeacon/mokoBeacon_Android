package com.moko.beacon.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.moko.beacon.BeaconConstants;
import com.moko.beacon.R;
import com.moko.beacon.databinding.ActivityConnectionModeBinding;
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

import java.util.HashMap;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class SetConnectionModeActivity extends BaseActivity {


    private ActivityConnectionModeBinding mBind;
    private HashMap<ViewGroup, View> viewHashMap;
    private String connectMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityConnectionModeBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        String connection_mode = getIntent().getStringExtra(BeaconConstants.EXTRA_KEY_DEVICE_CONNECTION_MODE);
        viewHashMap = new HashMap<>();
        viewHashMap.put(mBind.rlConnYes, mBind.ivConnYes);
        viewHashMap.put(mBind.rlConnNo, mBind.ivConnNo);
        if ("00".equals(connection_mode)) {
            setViewSelected(mBind.rlConnYes);
        } else {
            setViewSelected(mBind.rlConnNo);
        }
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
                ToastUtils.showToast(SetConnectionModeActivity.this, getString(R.string.alert_diconnected));
                SetConnectionModeActivity.this.setResult(BeaconConstants.RESULT_CONN_DISCONNECTED);
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
                    case CHAR_CONNECTION:
                        // 修改连接模式失败
                        ToastUtils.showToast(SetConnectionModeActivity.this, getString(R.string.read_data_failed));
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
                    case CHAR_CONNECTION:
                        // 修改连接模式成功
                        Intent i = new Intent();
                        i.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_CONNECTION_MODE, connectMode);
                        SetConnectionModeActivity.this.setResult(RESULT_OK, i);
                        if (EventBus.getDefault().isRegistered(this))
                            EventBus.getDefault().unregister(this);
                        finish();
                        break;
                }
            }
        });
    }

    private void setViewSelected(View parent) {
        for (View view : viewHashMap.values()) {
            ((ImageView) view).setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_unselected));
        }
        ((ImageView) viewHashMap.get(parent)).setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_selected));
        connectMode = (String) parent.getTag();
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
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setConnection(connectMode));
    }

    public void onConnectable(View view) {
        if (isWindowLocked()) return;
        setViewSelected(view);
    }
}

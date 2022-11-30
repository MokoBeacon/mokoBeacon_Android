package com.moko.beacon.activity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moko.beacon.BeaconConstants;
import com.moko.beacon.R;
import com.moko.beacon.databinding.ActivityTransmissionBinding;
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


public class SetTransmissionActivity extends BaseActivity {

    private ActivityTransmissionBinding mBind;
    private int transmissionGrade;
    private ArrayList<ViewGroup> mViews;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityTransmissionBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        int transmission = getIntent().getIntExtra(BeaconConstants.EXTRA_KEY_DEVICE_TRANSMISSION, 0);
        mViews = new ArrayList<>();
        mViews.add(mBind.llTransmissionGrade0);
        mViews.add(mBind.llTransmissionGrade1);
        mViews.add(mBind.llTransmissionGrade2);
        mViews.add(mBind.llTransmissionGrade3);
        mViews.add(mBind.llTransmissionGrade4);
        mViews.add(mBind.llTransmissionGrade5);
        mViews.add(mBind.llTransmissionGrade6);
        mViews.add(mBind.llTransmissionGrade7);
        setViewSeleceted(transmission);
        EventBus.getDefault().register(this);
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
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                ToastUtils.showToast(SetTransmissionActivity.this, getString(R.string.alert_diconnected));
                SetTransmissionActivity.this.setResult(BeaconConstants.RESULT_CONN_DISCONNECTED);
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
                    case CHAR_TRANSMISSION:
                        // 修改transmission失败
                        ToastUtils.showToast(SetTransmissionActivity.this, getString(R.string.read_data_failed));
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
                    case CHAR_TRANSMISSION:
                        // 修改transmission成功
                        SetTransmissionActivity.this.setResult(RESULT_OK);
                        finish();
                        break;
                }
            }
        });
    }

    private void setViewSeleceted(int transmission) {
        for (ViewGroup view : mViews) {
            view.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_radius_white_bg));
            ((TextView) view.getChildAt(0)).setTextColor(ContextCompat.getColor(this, R.color.blue_5691fc));
            ((TextView) view.getChildAt(1)).setTextColor(ContextCompat.getColor(this, R.color.grey_a6a6a6));
        }
        ViewGroup selected = mViews.get(transmission);
        selected.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_radius_blue_bg));
        ((TextView) selected.getChildAt(0)).setTextColor(ContextCompat.getColor(this, R.color.white_ffffff));
        ((TextView) selected.getChildAt(1)).setTextColor(ContextCompat.getColor(this, R.color.white_ffffff));
        transmissionGrade = transmission;
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
        if (transmissionGrade == 7) {
            transmissionGrade = 8;
        }
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setTransmission(transmissionGrade));
    }

    public void onTransmission(View view) {
        if (isWindowLocked()) return;
        int transmission = Integer.valueOf((String) view.getTag());
        setViewSeleceted(transmission);
    }
}

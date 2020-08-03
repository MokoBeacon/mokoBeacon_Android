package com.moko.beacon.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
 * @ClassPath com.moko.beacon.activity.SetTransmissionActivity
 */
public class SetTransmissionActivity extends BaseActivity {
    @Bind(R.id.ll_transmission_grade_0)
    LinearLayout llTransmissionGrade0;
    @Bind(R.id.ll_transmission_grade_1)
    LinearLayout llTransmissionGrade1;
    @Bind(R.id.ll_transmission_grade_2)
    LinearLayout llTransmissionGrade2;
    @Bind(R.id.ll_transmission_grade_3)
    LinearLayout llTransmissionGrade3;
    @Bind(R.id.ll_transmission_grade_4)
    LinearLayout llTransmissionGrade4;
    @Bind(R.id.ll_transmission_grade_5)
    LinearLayout llTransmissionGrade5;
    @Bind(R.id.ll_transmission_grade_6)
    LinearLayout llTransmissionGrade6;
    @Bind(R.id.ll_transmission_grade_7)
    LinearLayout llTransmissionGrade7;
    private int transmissionGrade;
    private ArrayList<ViewGroup> mViews;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transmission);
        ButterKnife.bind(this);
        int transmission = getIntent().getIntExtra(BeaconConstants.EXTRA_KEY_DEVICE_TRANSMISSION, 0);
        mViews = new ArrayList<>();
        mViews.add(llTransmissionGrade0);
        mViews.add(llTransmissionGrade1);
        mViews.add(llTransmissionGrade2);
        mViews.add(llTransmissionGrade3);
        mViews.add(llTransmissionGrade4);
        mViews.add(llTransmissionGrade5);
        mViews.add(llTransmissionGrade6);
        mViews.add(llTransmissionGrade7);
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
            if (MokoConstants.ACTION_CONN_STATUS_DISCONNECTED.equals(action)) {
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
                OrderType orderType = response.orderType;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderType) {
                    case TRANSMISSION:
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
                OrderType orderType = response.orderType;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderType) {
                    case TRANSMISSION:
                        // 修改transmission成功
                        SetTransmissionActivity.this.setResult(RESULT_OK);
                        finish();
                        break;
                }
            }
        });
    }


    @OnClick({R.id.tv_back, R.id.iv_save, R.id.ll_transmission_grade_0, R.id.ll_transmission_grade_1, R.id.ll_transmission_grade_2
            , R.id.ll_transmission_grade_3, R.id.ll_transmission_grade_4, R.id.ll_transmission_grade_5
            , R.id.ll_transmission_grade_6, R.id.ll_transmission_grade_7})
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
                if (transmissionGrade == 7) {
                    transmissionGrade = 8;
                }
                MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setTransmission(transmissionGrade));
                break;
            case R.id.ll_transmission_grade_0:
            case R.id.ll_transmission_grade_1:
            case R.id.ll_transmission_grade_2:
            case R.id.ll_transmission_grade_3:
            case R.id.ll_transmission_grade_4:
            case R.id.ll_transmission_grade_5:
            case R.id.ll_transmission_grade_6:
            case R.id.ll_transmission_grade_7:
                int transmission = Integer.valueOf((String) view.getTag());
                setViewSeleceted(transmission);
                break;

        }
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
}

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
import com.moko.beacon.service.MokoService;
import com.moko.beacon.utils.ToastUtils;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.entity.OrderType;

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
    @Bind(R.id.ll_transmission_grade_8)
    LinearLayout llTransmissionGrade8;
    private MokoService mMokoService;
    private int transmissionGrade;
    private ArrayList<ViewGroup> mViews;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transmission);
        ButterKnife.bind(this);
        bindService(new Intent(this, MokoService.class), mServiceConnection, BIND_AUTO_CREATE);
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
        mViews.add(llTransmissionGrade8);
        setViewSeleceted(transmission);
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
                    ToastUtils.showToast(SetTransmissionActivity.this, getString(R.string.alert_diconnected));
                    SetTransmissionActivity.this.setResult(BeaconConstants.RESULT_CONN_DISCONNECTED);
                    finish();
                }
                if (MokoConstants.ACTION_RESPONSE_TIMEOUT.equals(action)) {
                    OrderType orderType = (OrderType) intent.getSerializableExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TYPE);
                    switch (orderType) {
                        case transmission:
                            // 修改transmission失败
                            ToastUtils.showToast(SetTransmissionActivity.this, getString(R.string.read_data_failed));
                            finish();
                            break;
                    }
                }
                if (MokoConstants.ACTION_RESPONSE_SUCCESS.equals(action)) {
                    OrderType orderType = (OrderType) intent.getSerializableExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TYPE);
                    switch (orderType) {
                        case transmission:
                            // 修改transmission成功
                            Intent i = new Intent();
                            i.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_TRANSMISSION, transmissionGrade);
                            SetTransmissionActivity.this.setResult(RESULT_OK, i);
                            finish();
                            break;
                    }
                }
            }
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMokoService = ((MokoService.LocalBinder) service).getService();
            // 注册广播接收器
            IntentFilter filter = new IntentFilter();
            filter.addAction(MokoConstants.ACTION_CONNECT_SUCCESS);
            filter.addAction(MokoConstants.ACTION_CONNECT_DISCONNECTED);
            filter.addAction(MokoConstants.ACTION_RESPONSE_SUCCESS);
            filter.addAction(MokoConstants.ACTION_RESPONSE_TIMEOUT);
            filter.addAction(MokoConstants.ACTION_RESPONSE_FINISH);
            filter.setPriority(300);
            registerReceiver(mReceiver, filter);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @OnClick({R.id.tv_back, R.id.iv_save, R.id.ll_transmission_grade_0, R.id.ll_transmission_grade_1, R.id.ll_transmission_grade_2
            , R.id.ll_transmission_grade_3, R.id.ll_transmission_grade_4, R.id.ll_transmission_grade_5
            , R.id.ll_transmission_grade_6, R.id.ll_transmission_grade_7, R.id.ll_transmission_grade_8})
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
                mMokoService.sendOrder(mMokoService.setTransmission(transmissionGrade));
                break;
            case R.id.ll_transmission_grade_0:
            case R.id.ll_transmission_grade_1:
            case R.id.ll_transmission_grade_2:
            case R.id.ll_transmission_grade_3:
            case R.id.ll_transmission_grade_4:
            case R.id.ll_transmission_grade_5:
            case R.id.ll_transmission_grade_6:
            case R.id.ll_transmission_grade_7:
            case R.id.ll_transmission_grade_8:
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
            ((TextView) view.getChildAt(2)).setTextColor(ContextCompat.getColor(this, R.color.grey_a6a6a6));
        }
        ViewGroup selected = mViews.get(transmission);
        selected.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_radius_blue_bg));
        ((TextView) selected.getChildAt(0)).setTextColor(ContextCompat.getColor(this, R.color.white_ffffff));
        ((TextView) selected.getChildAt(1)).setTextColor(ContextCompat.getColor(this, R.color.white_ffffff));
        ((TextView) selected.getChildAt(2)).setTextColor(ContextCompat.getColor(this, R.color.white_ffffff));
        transmissionGrade = transmission;
    }
}

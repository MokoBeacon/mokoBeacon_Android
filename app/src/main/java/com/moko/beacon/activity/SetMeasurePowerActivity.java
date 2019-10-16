package com.moko.beacon.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.moko.beacon.BeaconConstants;
import com.moko.beacon.R;
import com.moko.beacon.service.MokoService;
import com.moko.beacon.utils.ToastUtils;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.entity.OrderType;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @Date 2017/12/18 0018
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beacon.activity.SetMeasurePowerActivity
 */
public class SetMeasurePowerActivity extends BaseActivity {
    @Bind(R.id.et_measure_power)
    EditText etMeasurePower;
    private MokoService mMokoService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure_power);
        ButterKnife.bind(this);
        bindService(new Intent(this, MokoService.class), mServiceConnection, BIND_AUTO_CREATE);
        int measure_power = getIntent().getIntExtra(BeaconConstants.EXTRA_KEY_DEVICE_MEASURE_POWER, 0);
        etMeasurePower.setText(measure_power + "");
        etMeasurePower.setSelection(String.valueOf(measure_power).length());
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
                    ToastUtils.showToast(SetMeasurePowerActivity.this, getString(R.string.alert_diconnected));
                    SetMeasurePowerActivity.this.setResult(BeaconConstants.RESULT_CONN_DISCONNECTED);
                    finish();
                }
                if (MokoConstants.ACTION_RESPONSE_TIMEOUT.equals(action)) {
                    OrderType orderType = (OrderType) intent.getSerializableExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TYPE);
                    switch (orderType) {
                        case measurePower:
                            // 修改measure power失败
                            ToastUtils.showToast(SetMeasurePowerActivity.this, getString(R.string.read_data_failed));
                            finish();
                            break;
                    }
                }
                if (MokoConstants.ACTION_RESPONSE_SUCCESS.equals(action)) {
                    OrderType orderType = (OrderType) intent.getSerializableExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TYPE);
                    switch (orderType) {
                        case measurePower:
                            // 修改measure power成功
                            Intent i = new Intent();
                            i.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_MEASURE_POWER, Integer.valueOf(etMeasurePower.getText().toString()));
                            SetMeasurePowerActivity.this.setResult(RESULT_OK, i);
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

    @OnClick({R.id.tv_back, R.id.iv_save})
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
                if (TextUtils.isEmpty(etMeasurePower.getText().toString())) {
                    ToastUtils.showToast(this, getString(R.string.alert_data_cannot_null));
                    return;
                }
                int power = Integer.valueOf(etMeasurePower.getText().toString());
                if (power < 0 || power > 119) {
                    ToastUtils.showToast(this, getString(R.string.alert_measure_power_range));
                    return;
                }
                mMokoService.sendOrder(mMokoService.setMeasurePower(Integer.valueOf(etMeasurePower.getText().toString())));
                break;

        }
    }
}

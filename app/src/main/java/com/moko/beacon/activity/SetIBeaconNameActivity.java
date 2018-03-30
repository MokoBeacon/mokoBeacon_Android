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
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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
 * @ClassPath com.moko.beacon.activity.SetIBeaconNameActivity
 */
public class SetIBeaconNameActivity extends BaseActivity {
    @Bind(R.id.et_ibeacon_name)
    EditText etIBeaconName;
    @Bind(R.id.tv_tips)
    TextView tvTips;
    private MokoService mMokoService;
    private boolean isSupportThreeAxis;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_name);
        ButterKnife.bind(this);
        bindService(new Intent(this, MokoService.class), mServiceConnection, BIND_AUTO_CREATE);
        String ibeaconName = getIntent().getStringExtra(BeaconConstants.EXTRA_KEY_DEVICE_IBEACON_NAME);
        String ibeaconThreeAxis = getIntent().getStringExtra(BeaconConstants.EXTRA_KEY_DEVICE_IBEACON_THREE_AXIS);
        isSupportThreeAxis = !TextUtils.isEmpty(ibeaconThreeAxis);
        ibeaconName = ibeaconName.trim();
        InputFilter[] filters = {new InputFilter.LengthFilter(isSupportThreeAxis ? 4 : 10)};
        etIBeaconName.setFilters(filters);
        etIBeaconName.setText(ibeaconName);
        etIBeaconName.setSelection(String.valueOf(ibeaconName).length());
        tvTips.setText(isSupportThreeAxis ? getString(R.string.tips_ibeacon_name_three_axis) : getString(R.string.tips_ibeacon_name));
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
                    ToastUtils.showToast(SetIBeaconNameActivity.this, getString(R.string.alert_diconnected));
                    SetIBeaconNameActivity.this.setResult(BeaconConstants.RESULT_CONN_DISCONNECTED);
                    finish();
                }
                if (MokoConstants.ACTION_RESPONSE_TIMEOUT.equals(action)) {
                    OrderType orderType = (OrderType) intent.getSerializableExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TYPE);
                    switch (orderType) {
                        case iBeaconName:
                            // 修改ibeacon name失败
                            ToastUtils.showToast(SetIBeaconNameActivity.this, getString(R.string.read_data_failed));
                            finish();
                            break;
                    }
                }
                if (MokoConstants.ACTION_RESPONSE_SUCCESS.equals(action)) {
                    OrderType orderType = (OrderType) intent.getSerializableExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TYPE);
                    switch (orderType) {
                        case iBeaconName:
                            // 修改ibeacon name成功
                            Intent i = new Intent();
                            i.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_IBEACON_NAME, etIBeaconName.getText().toString());
                            SetIBeaconNameActivity.this.setResult(RESULT_OK, i);
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
                if (TextUtils.isEmpty(etIBeaconName.getText().toString())) {
                    ToastUtils.showToast(this, getString(R.string.alert_data_cannot_null));
                    return;
                }
                if (etIBeaconName.getText().toString().length() > (isSupportThreeAxis ? 4 : 10)) {
                    ToastUtils.showToast(this, isSupportThreeAxis ? getString(R.string.tips_ibeacon_name_three_axis) : getString(R.string.tips_ibeacon_name));
                    return;
                }
                mMokoService.sendOrder(mMokoService.setIBeaconName(etIBeaconName.getText().toString()));
                break;

        }
    }
}

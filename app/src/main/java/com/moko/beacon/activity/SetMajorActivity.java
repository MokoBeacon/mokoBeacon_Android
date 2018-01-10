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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.moko.beacon.BeaconConstants;
import com.moko.beacon.R;
import com.moko.beacon.service.BeaconService;
import com.moko.beacon.utils.ToastUtils;
import com.moko.support.MokoConstants;
import com.moko.support.entity.OrderType;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @Date 2017/12/18 0018
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beacon.activity.SetMajorActivity
 */
public class SetMajorActivity extends BaseActivity {
    @Bind(R.id.et_major)
    EditText etMajor;
    @Bind(R.id.tv_decimalism)
    TextView tvDecimalism;
    @Bind(R.id.tv_hexadecimal)
    TextView tvHexadecimal;
    private BeaconService mBeaconService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_major);
        ButterKnife.bind(this);
        bindService(new Intent(this, BeaconService.class), mServiceConnection, BIND_AUTO_CREATE);
        int major = getIntent().getIntExtra(BeaconConstants.EXTRA_KEY_DEVICE_MAJOR, 0);
        etMajor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString())) {
                    tvDecimalism.setText("");
                    tvHexadecimal.setText("");
                    return;
                }
                tvDecimalism.setText(s.toString());
                tvHexadecimal.setText(Integer.toHexString(Integer.parseInt(s.toString())));
            }
        });
        etMajor.setText(major + "");
        etMajor.setSelection(String.valueOf(major).length());
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
                    ToastUtils.showToast(SetMajorActivity.this, getString(R.string.alert_diconnected));
                    SetMajorActivity.this.setResult(BeaconConstants.RESULT_CONN_DISCONNECTED);
                    finish();
                }
                if (MokoConstants.ACTION_RESPONSE_TIMEOUT.equals(action)) {
                    OrderType orderType = (OrderType) intent.getSerializableExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TYPE);
                    switch (orderType) {
                        case major:
                            // 修改major失败
                            ToastUtils.showToast(SetMajorActivity.this, getString(R.string.read_data_failed));
                            finish();
                            break;
                    }
                }
                if (MokoConstants.ACTION_RESPONSE_SUCCESS.equals(action)) {
                    OrderType orderType = (OrderType) intent.getSerializableExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TYPE);
                    switch (orderType) {
                        case major:
                            // 修改major成功
                            Intent i = new Intent();
                            i.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_MAJOR, Integer.valueOf(etMajor.getText().toString()));
                            SetMajorActivity.this.setResult(RESULT_OK, i);
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
            mBeaconService = ((BeaconService.LocalBinder) service).getService();
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
                if (TextUtils.isEmpty(etMajor.getText().toString())) {
                    ToastUtils.showToast(this, getString(R.string.alert_data_cannot_null));
                    return;
                }
                if (Integer.valueOf(etMajor.getText().toString()) > 65535 || Integer.valueOf(etMajor.getText().toString()) < 0) {
                    ToastUtils.showToast(this, getString(R.string.alert_minor_range));
                    return;
                }
                mBeaconService.sendOrder(mBeaconService.setMajor(Integer.valueOf(etMajor.getText().toString())));
                break;

        }
    }
}

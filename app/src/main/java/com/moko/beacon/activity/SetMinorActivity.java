package com.moko.beacon.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
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

import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @Date 2017/12/18 0018
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beacon.activity.SetMinorActivity
 */
public class SetMinorActivity extends BaseActivity {
    @BindView(R.id.et_minor)
    EditText etMinor;
    @BindView(R.id.tv_decimalism)
    TextView tvDecimalism;
    @BindView(R.id.tv_hexadecimal)
    TextView tvHexadecimal;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minor);
        ButterKnife.bind(this);
        int minor = getIntent().getIntExtra(BeaconConstants.EXTRA_KEY_DEVICE_MINOR, 0);
        etMinor.addTextChangedListener(new TextWatcher() {
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
        etMinor.setText(minor + "");
        etMinor.setSelection(String.valueOf(minor).length());
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
                ToastUtils.showToast(SetMinorActivity.this, getString(R.string.alert_diconnected));
                SetMinorActivity.this.setResult(BeaconConstants.RESULT_CONN_DISCONNECTED);
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
                    case MINOR:
                        // 修改minor失败
                        ToastUtils.showToast(SetMinorActivity.this, getString(R.string.read_data_failed));
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
                    case MINOR:
                        // 修改minor成功
                        Intent i = new Intent();
                        i.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_MINOR, Integer.valueOf(etMinor.getText().toString()));
                        SetMinorActivity.this.setResult(RESULT_OK, i);
                        finish();
                        break;
                }
            }
        });
    }

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
                if (TextUtils.isEmpty(etMinor.getText().toString())) {
                    ToastUtils.showToast(this, getString(R.string.alert_data_cannot_null));
                    return;
                }
                if (Integer.valueOf(etMinor.getText().toString()) > 65535 || Integer.valueOf(etMinor.getText().toString()) < 0) {
                    ToastUtils.showToast(this, getString(R.string.alert_minor_range));
                    return;
                }
                MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setMinor(Integer.valueOf(etMinor.getText().toString())));
                break;

        }
    }
}

package com.moko.beacon.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

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
 * @ClassPath com.moko.beacon.activity.SetDeviceIdActivity
 */
public class SetDeviceIdActivity extends BaseActivity {
    @BindView(R.id.et_device_id)
    EditText etDeviceId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_id);
        ButterKnife.bind(this);
        String deviceId = getIntent().getStringExtra(BeaconConstants.EXTRA_KEY_DEVICE_DEVICE_ID);

        etDeviceId.setText(deviceId);
        etDeviceId.setSelection(String.valueOf(deviceId).length());
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
                ToastUtils.showToast(SetDeviceIdActivity.this, getString(R.string.alert_diconnected));
                SetDeviceIdActivity.this.setResult(BeaconConstants.RESULT_CONN_DISCONNECTED);
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
                    case SERIAL_ID:
                        // 修改deviceId失败
                        ToastUtils.showToast(SetDeviceIdActivity.this, getString(R.string.read_data_failed));
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
                    case SERIAL_ID:
                        // 修改deviceId成功
                        Intent i = new Intent();
                        i.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_DEVICE_ID, etDeviceId.getText().toString());
                        SetDeviceIdActivity.this.setResult(RESULT_OK, i);
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
                if (TextUtils.isEmpty(etDeviceId.getText().toString())) {
                    ToastUtils.showToast(this, getString(R.string.alert_data_cannot_null));
                    return;
                }
                if (etDeviceId.getText().toString().length() > 5) {
                    ToastUtils.showToast(this, getString(R.string.tips_device_id));
                    return;
                }
                MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setSerialID(etDeviceId.getText().toString()));
                break;

        }
    }
}

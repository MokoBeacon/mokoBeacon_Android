package com.moko.beacon.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;

import com.moko.beacon.BeaconConstants;
import com.moko.beacon.R;
import com.moko.beacon.databinding.ActivityDeviceNameBinding;
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

public class SetIBeaconNameActivity extends BaseActivity {

    private ActivityDeviceNameBinding mBind;
    private boolean isSupportThreeAxis;
    private final String FILTER_ASCII = "\\A\\p{ASCII}*\\z";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityDeviceNameBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        String ibeaconName = getIntent().getStringExtra(BeaconConstants.EXTRA_KEY_DEVICE_IBEACON_NAME);
        String ibeaconThreeAxis = getIntent().getStringExtra(BeaconConstants.EXTRA_KEY_DEVICE_IBEACON_THREE_AXIS);
        isSupportThreeAxis = !TextUtils.isEmpty(ibeaconThreeAxis);
        ibeaconName = ibeaconName.trim();
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (!(source + "").matches(FILTER_ASCII)) {
                    return "";
                }

                return null;
            }
        };
        InputFilter[] filters = {new InputFilter.LengthFilter(isSupportThreeAxis ? 4 : 10), filter};
        mBind.etIbeaconName.setFilters(filters);
        mBind.etIbeaconName.setText(ibeaconName);
        mBind.etIbeaconName.setSelection(String.valueOf(ibeaconName).length());
        mBind.tvTips.setText(isSupportThreeAxis ? getString(R.string.tips_ibeacon_name_three_axis) : getString(R.string.tips_ibeacon_name));
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
                ToastUtils.showToast(SetIBeaconNameActivity.this, getString(R.string.alert_diconnected));
                SetIBeaconNameActivity.this.setResult(BeaconConstants.RESULT_CONN_DISCONNECTED);
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
                    case CHAR_ADV_NAME:
                        // 修改ibeacon name失败
                        ToastUtils.showToast(SetIBeaconNameActivity.this, getString(R.string.read_data_failed));
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
                    case CHAR_ADV_NAME:
                        // 修改ibeacon name成功
                        Intent i = new Intent();
                        i.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_IBEACON_NAME, mBind.etIbeaconName.getText().toString());
                        SetIBeaconNameActivity.this.setResult(RESULT_OK, i);
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
        if (TextUtils.isEmpty(mBind.etIbeaconName.getText().toString())) {
            ToastUtils.showToast(this, getString(R.string.alert_data_cannot_null));
            return;
        }
        if (mBind.etIbeaconName.getText().toString().length() > (isSupportThreeAxis ? 4 : 10)) {
            ToastUtils.showToast(this, isSupportThreeAxis ? getString(R.string.tips_ibeacon_name_three_axis) : getString(R.string.tips_ibeacon_name));
            return;
        }
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setAdvName(mBind.etIbeaconName.getText().toString()));
    }
}

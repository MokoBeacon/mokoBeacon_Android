package com.moko.beacon.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
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
 * @ClassPath com.moko.beacon.activity.SetIBeaconNameActivity
 */
public class SetIBeaconNameActivity extends BaseActivity {
    @BindView(R.id.et_ibeacon_name)
    EditText etIBeaconName;
    @BindView(R.id.tv_tips)
    TextView tvTips;
    private boolean isSupportThreeAxis;
    private final String FILTER_ASCII = "\\A\\p{ASCII}*\\z";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_name);
        ButterKnife.bind(this);
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
        etIBeaconName.setFilters(filters);
        etIBeaconName.setText(ibeaconName);
        etIBeaconName.setSelection(String.valueOf(ibeaconName).length());
        tvTips.setText(isSupportThreeAxis ? getString(R.string.tips_ibeacon_name_three_axis) : getString(R.string.tips_ibeacon_name));
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
                OrderType orderType = response.orderType;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderType) {
                    case ADV_NAME:
                        // 修改ibeacon name失败
                        ToastUtils.showToast(SetIBeaconNameActivity.this, getString(R.string.read_data_failed));
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
                    case ADV_NAME:
                        // 修改ibeacon name成功
                        Intent i = new Intent();
                        i.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_IBEACON_NAME, etIBeaconName.getText().toString());
                        SetIBeaconNameActivity.this.setResult(RESULT_OK, i);
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
                if (TextUtils.isEmpty(etIBeaconName.getText().toString())) {
                    ToastUtils.showToast(this, getString(R.string.alert_data_cannot_null));
                    return;
                }
                if (etIBeaconName.getText().toString().length() > (isSupportThreeAxis ? 4 : 10)) {
                    ToastUtils.showToast(this, isSupportThreeAxis ? getString(R.string.tips_ibeacon_name_three_axis) : getString(R.string.tips_ibeacon_name));
                    return;
                }
                MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setAdvName(etIBeaconName.getText().toString()));
                break;

        }
    }
}

package com.moko.beacon.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import com.moko.beacon.BeaconConstants;
import com.moko.beacon.R;
import com.moko.beacon.databinding.ActivityUuidBinding;
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

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.Nullable;

public class SetUUIDActivity extends BaseActivity {

    public static final String UUID_PATTERN = "[A-Fa-f0-9]{8}-(?:[A-Fa-f0-9]{4}-){3}[A-Fa-f0-9]{12}";

    private ActivityUuidBinding mBind;
    private HashMap<Integer, View> mUUIDViews;
    private Pattern pattern;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityUuidBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        String uuid = getIntent().getStringExtra(BeaconConstants.EXTRA_KEY_DEVICE_UUID);
        mBind.tvUuid.setText(uuid);
        pattern = Pattern.compile(UUID_PATTERN);
        mUUIDViews = new HashMap<>();
        mUUIDViews.put(R.id.rl_airLocate, mBind.ivAirLocateSelected);
        mUUIDViews.put(R.id.rl_wechat_1, mBind.ivWechat1Selected);
        mUUIDViews.put(R.id.rl_wechat_2, mBind.ivWechat2Selected);
        mUUIDViews.put(R.id.rl_estimote, mBind.ivEstimoteSelected);
        mUUIDViews.put(R.id.rl_uuid, mBind.ivUuidSelected);
        mBind.etSeletcedUuid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                if (!pattern.matcher(input).matches()) {
                    if (input.length() == 9 && !input.endsWith("-")) {
                        String show = input.substring(0, 8) + "-" + input.substring(8, input.length());
                        mBind.etSeletcedUuid.setText(show);
                        mBind.etSeletcedUuid.setSelection(show.length());
                    }
                    if (input.length() == 14 && !input.endsWith("-")) {
                        String show = input.substring(0, 13) + "-" + input.substring(13, input.length());
                        mBind.etSeletcedUuid.setText(show);
                        mBind.etSeletcedUuid.setSelection(show.length());
                    }
                    if (input.length() == 19 && !input.endsWith("-")) {
                        String show = input.substring(0, 18) + "-" + input.substring(18, input.length());
                        mBind.etSeletcedUuid.setText(show);
                        mBind.etSeletcedUuid.setSelection(show.length());
                    }
                    if (input.length() == 24 && !input.endsWith("-")) {
                        String show = input.substring(0, 23) + "-" + input.substring(23, input.length());
                        mBind.etSeletcedUuid.setText(show);
                        mBind.etSeletcedUuid.setSelection(show.length());
                    }
                }
            }
        });
        setUUIDSelected(mBind.rlUuid);
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
                ToastUtils.showToast(SetUUIDActivity.this, getString(R.string.alert_diconnected));
                SetUUIDActivity.this.setResult(BeaconConstants.RESULT_CONN_DISCONNECTED);
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
                    case CHAR_DEVICE_UUID:
                        // 修改UUID失败
                        ToastUtils.showToast(SetUUIDActivity.this, getString(R.string.read_data_failed));
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
                    case CHAR_DEVICE_UUID:
                        // 修改UUID成功
                        Intent i = new Intent();
                        i.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_UUID, mBind.etSeletcedUuid.getText().toString());
                        SetUUIDActivity.this.setResult(RESULT_OK, i);
                        if (EventBus.getDefault().isRegistered(this))
                            EventBus.getDefault().unregister(this);
                        finish();
                        break;
                }
            }
        });
    }

    private void setUUIDSelected(View v) {
        for (View view : mUUIDViews.values()) {
            view.setVisibility(View.GONE);
        }
        View view = mUUIDViews.get(v.getId());
        view.setVisibility(View.VISIBLE);
        switch (v.getId()) {
            case R.id.rl_airLocate:
                mBind.etSeletcedUuid.setText(mBind.tvAirLocateUuid.getText().toString());
                mBind.etSeletcedUuid.setSelection(mBind.tvAirLocateUuid.getText().toString().length());
                break;
            case R.id.rl_wechat_1:
                mBind.etSeletcedUuid.setText(mBind.tvWechat1Uuid.getText().toString());
                mBind.etSeletcedUuid.setSelection(mBind.tvWechat1Uuid.getText().toString().length());
                break;
            case R.id.rl_wechat_2:
                mBind.etSeletcedUuid.setText(mBind.tvWechat2Uuid.getText().toString());
                mBind.etSeletcedUuid.setSelection(mBind.tvWechat2Uuid.getText().toString().length());
                break;
            case R.id.rl_estimote:
                mBind.etSeletcedUuid.setText(mBind.tvEstimoteUuid.getText().toString());
                mBind.etSeletcedUuid.setSelection(mBind.tvEstimoteUuid.getText().toString().length());
                break;
            case R.id.rl_uuid:
                mBind.etSeletcedUuid.setText(mBind.tvUuid.getText().toString());
                mBind.etSeletcedUuid.setSelection(mBind.tvUuid.getText().toString().length());
                break;
        }
    }

    private boolean isUUID(String etUUid) {
        Matcher m = pattern.matcher(etUUid);
        return m.matches();
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
        String uuid = mBind.etSeletcedUuid.getText().toString();
        if (TextUtils.isEmpty(uuid)) {
            ToastUtils.showToast(this, getString(R.string.alert_data_cannot_null));
            return;
        }
        if (isUUID(uuid)) {
            // 设置UUID
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setDeviceUUID(uuid));
        } else {
            ToastUtils.showToast(this, "uuid is nonstandard");
        }
    }

    public void onUUID(View view) {
        if (isWindowLocked()) return;
        setUUIDSelected(view);
    }
}

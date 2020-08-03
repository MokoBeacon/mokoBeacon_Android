package com.moko.beacon.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @Date 2017/12/15 0015
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beacon.activity.SetUUIDActivity
 */
public class SetUUIDActivity extends BaseActivity {

    public static final String UUID_PATTERN = "[A-Fa-f0-9]{8}-(?:[A-Fa-f0-9]{4}-){3}[A-Fa-f0-9]{12}";

    @Bind(R.id.et_seletced_uuid)
    EditText etSeletcedUuid;
    @Bind(R.id.tv_airLocate_uuid)
    TextView tvAirLocateUuid;
    @Bind(R.id.iv_airLocate_selected)
    ImageView ivAirLocateSelected;
    @Bind(R.id.tv_wechat_1_uuid)
    TextView tvWechat1Uuid;
    @Bind(R.id.iv_wechat_1_selected)
    ImageView ivWechat1Selected;
    @Bind(R.id.tv_wechat_2_uuid)
    TextView tvWechat2Uuid;
    @Bind(R.id.iv_wechat_2_selected)
    ImageView ivWechat2Selected;
    @Bind(R.id.tv_estimote_uuid)
    TextView tvEstimoteUuid;
    @Bind(R.id.iv_estimote_selected)
    ImageView ivEstimoteSelected;
    @Bind(R.id.tv_uuid)
    TextView tvUuid;
    @Bind(R.id.iv_uuid_selected)
    ImageView ivUuidSelected;
    @Bind(R.id.rl_uuid)
    RelativeLayout rlUuid;
    private HashMap<Integer, View> mUUIDViews;
    private Pattern pattern;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uuid);
        ButterKnife.bind(this);
        String uuid = getIntent().getStringExtra(BeaconConstants.EXTRA_KEY_DEVICE_UUID);
        tvUuid.setText(uuid);
        pattern = Pattern.compile(UUID_PATTERN);
        mUUIDViews = new HashMap<>();
        mUUIDViews.put(R.id.rl_airLocate, ivAirLocateSelected);
        mUUIDViews.put(R.id.rl_wechat_1, ivWechat1Selected);
        mUUIDViews.put(R.id.rl_wechat_2, ivWechat2Selected);
        mUUIDViews.put(R.id.rl_estimote, ivEstimoteSelected);
        mUUIDViews.put(R.id.rl_uuid, ivUuidSelected);
        etSeletcedUuid.addTextChangedListener(new TextWatcher() {
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
                        etSeletcedUuid.setText(show);
                        etSeletcedUuid.setSelection(show.length());
                    }
                    if (input.length() == 14 && !input.endsWith("-")) {
                        String show = input.substring(0, 13) + "-" + input.substring(13, input.length());
                        etSeletcedUuid.setText(show);
                        etSeletcedUuid.setSelection(show.length());
                    }
                    if (input.length() == 19 && !input.endsWith("-")) {
                        String show = input.substring(0, 18) + "-" + input.substring(18, input.length());
                        etSeletcedUuid.setText(show);
                        etSeletcedUuid.setSelection(show.length());
                    }
                    if (input.length() == 24 && !input.endsWith("-")) {
                        String show = input.substring(0, 23) + "-" + input.substring(23, input.length());
                        etSeletcedUuid.setText(show);
                        etSeletcedUuid.setSelection(show.length());
                    }
                }
            }
        });
        setUUIDSelected(rlUuid);
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
                OrderType orderType = response.orderType;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderType) {
                    case DEVICE_UUID:
                        // 修改UUID失败
                        ToastUtils.showToast(SetUUIDActivity.this, getString(R.string.read_data_failed));
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
                    case DEVICE_UUID:
                        // 修改UUID成功
                        Intent i = new Intent();
                        i.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_UUID, etSeletcedUuid.getText().toString());
                        SetUUIDActivity.this.setResult(RESULT_OK, i);
                        finish();
                        break;
                }
            }
        });
    }

    @OnClick({R.id.tv_back, R.id.iv_save, R.id.rl_airLocate, R.id.rl_wechat_1, R.id.rl_wechat_2, R.id.rl_estimote, R.id.rl_uuid})
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
                String uuid = etSeletcedUuid.getText().toString();
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
                break;
            case R.id.rl_airLocate:
            case R.id.rl_wechat_1:
            case R.id.rl_wechat_2:
            case R.id.rl_estimote:
            case R.id.rl_uuid:
                setUUIDSelected(view);
                break;
        }
    }

    private void setUUIDSelected(View v) {
        for (View view : mUUIDViews.values()) {
            view.setVisibility(View.GONE);
        }
        View view = mUUIDViews.get(v.getId());
        view.setVisibility(View.VISIBLE);
        switch (v.getId()) {
            case R.id.rl_airLocate:
                etSeletcedUuid.setText(tvAirLocateUuid.getText().toString());
                etSeletcedUuid.setSelection(tvAirLocateUuid.getText().toString().length());
                break;
            case R.id.rl_wechat_1:
                etSeletcedUuid.setText(tvWechat1Uuid.getText().toString());
                etSeletcedUuid.setSelection(tvWechat1Uuid.getText().toString().length());
                break;
            case R.id.rl_wechat_2:
                etSeletcedUuid.setText(tvWechat2Uuid.getText().toString());
                etSeletcedUuid.setSelection(tvWechat2Uuid.getText().toString().length());
                break;
            case R.id.rl_estimote:
                etSeletcedUuid.setText(tvEstimoteUuid.getText().toString());
                etSeletcedUuid.setSelection(tvEstimoteUuid.getText().toString().length());
                break;
            case R.id.rl_uuid:
                etSeletcedUuid.setText(tvUuid.getText().toString());
                etSeletcedUuid.setSelection(tvUuid.getText().toString().length());
                break;
        }
    }

    private boolean isUUID(String etUUid) {
        Matcher m = pattern.matcher(etUUid);
        return m.matches();
    }

}

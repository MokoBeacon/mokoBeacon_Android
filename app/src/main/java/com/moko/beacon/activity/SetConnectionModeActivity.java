package com.moko.beacon.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

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

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @Date 2017/12/18 0018
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beacon.activity.SetIBeaconNameActivity
 */
public class SetConnectionModeActivity extends BaseActivity {

    @Bind(R.id.iv_conn_yes)
    ImageView ivConnYes;
    @Bind(R.id.rl_conn_yes)
    RelativeLayout rlConnYes;
    @Bind(R.id.iv_conn_no)
    ImageView ivConnNo;
    @Bind(R.id.rl_conn_no)
    RelativeLayout rlConnNo;
    private HashMap<ViewGroup, View> viewHashMap;
    private String connectMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_mode);
        ButterKnife.bind(this);
        String connection_mode = getIntent().getStringExtra(BeaconConstants.EXTRA_KEY_DEVICE_CONNECTION_MODE);
        viewHashMap = new HashMap<>();
        viewHashMap.put(rlConnYes, ivConnYes);
        viewHashMap.put(rlConnNo, ivConnNo);
        if ("00".equals(connection_mode)) {
            setViewSelected(rlConnYes);
        } else {
            setViewSelected(rlConnNo);
        }
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
                ToastUtils.showToast(SetConnectionModeActivity.this, getString(R.string.alert_diconnected));
                SetConnectionModeActivity.this.setResult(BeaconConstants.RESULT_CONN_DISCONNECTED);
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
                    case CONNECTION:
                        // 修改连接模式失败
                        ToastUtils.showToast(SetConnectionModeActivity.this, getString(R.string.read_data_failed));
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
                    case CONNECTION:
                        // 修改连接模式成功
                        Intent i = new Intent();
                        i.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_CONNECTION_MODE, connectMode);
                        SetConnectionModeActivity.this.setResult(RESULT_OK, i);
                        finish();
                        break;
                }
            }
        });
    }

    @OnClick({R.id.tv_back, R.id.iv_save, R.id.rl_conn_yes, R.id.rl_conn_no})
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
                MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setConnection(connectMode));
                break;
            case R.id.rl_conn_yes:
            case R.id.rl_conn_no:
                setViewSelected(view);
                break;
        }
    }

    private void setViewSelected(View parent) {
        for (View view : viewHashMap.values()) {
            ((ImageView) view).setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_unselected));
        }
        ((ImageView) viewHashMap.get(parent)).setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_selected));
        connectMode = (String) parent.getTag();
    }
}

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
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.moko.beacon.BeaconConstants;
import com.moko.beacon.R;
import com.moko.beacon.service.MokoService;
import com.moko.beacon.utils.ToastUtils;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.entity.OrderType;

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
    private MokoService mMokoService;
    private HashMap<ViewGroup, View> viewHashMap;
    private String connectMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_mode);
        ButterKnife.bind(this);
        bindService(new Intent(this, MokoService.class), mServiceConnection, BIND_AUTO_CREATE);
        String connection_mode = getIntent().getStringExtra(BeaconConstants.EXTRA_KEY_DEVICE_CONNECTION_MODE);
        viewHashMap = new HashMap<>();
        viewHashMap.put(rlConnYes, ivConnYes);
        viewHashMap.put(rlConnNo, ivConnNo);
        if ("00".equals(connection_mode)) {
            setViewSelected(rlConnYes);
        } else {
            setViewSelected(rlConnNo);
        }
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
                    ToastUtils.showToast(SetConnectionModeActivity.this, getString(R.string.alert_diconnected));
                    SetConnectionModeActivity.this.setResult(BeaconConstants.RESULT_CONN_DISCONNECTED);
                    finish();
                }
                if (MokoConstants.ACTION_RESPONSE_TIMEOUT.equals(action)) {
                    OrderType orderType = (OrderType) intent.getSerializableExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TYPE);
                    switch (orderType) {
                        case connectionMode:
                            // 修改连接模式失败
                            ToastUtils.showToast(SetConnectionModeActivity.this, getString(R.string.read_data_failed));
                            finish();
                            break;
                    }
                }
                if (MokoConstants.ACTION_RESPONSE_SUCCESS.equals(action)) {
                    OrderType orderType = (OrderType) intent.getSerializableExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TYPE);
                    switch (orderType) {
                        case connectionMode:
                            // 修改连接模式成功
                            Intent i = new Intent();
                            i.putExtra(BeaconConstants.EXTRA_KEY_DEVICE_CONNECTION_MODE, connectMode);
                            SetConnectionModeActivity.this.setResult(RESULT_OK, i);
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
                mMokoService.sendOrder(mMokoService.setConnectionMode(connectMode));
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

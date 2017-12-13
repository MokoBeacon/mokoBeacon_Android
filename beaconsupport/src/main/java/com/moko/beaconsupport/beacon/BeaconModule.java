package com.moko.beaconsupport.beacon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Message;
import android.text.TextUtils;

import com.moko.beaconsupport.callback.BeaconConnStateCallback;
import com.moko.beaconsupport.callback.ScanDeviceCallback;
import com.moko.beaconsupport.handler.BaseHandler;
import com.moko.beaconsupport.handler.BeaconConnStateHandler;
import com.moko.beaconsupport.handler.BeaconLeScanHandler;
import com.moko.beaconsupport.handler.BeaconResponseHandler;
import com.moko.beaconsupport.log.LogModule;
import com.moko.beaconsupport.utils.BleConnectionCompat;

import java.lang.reflect.Method;

/**
 * @Date 2017/12/7 0007
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beaconsupport.beacon.BeaconModule
 */
public class BeaconModule {
    public static final int HANDLER_MESSAGE_WHAT_CONNECTED = 1;
    public static final int HANDLER_MESSAGE_WHAT_DISCONNECTED = 2;
    public static final int HANDLER_MESSAGE_WHAT_SERVICES_DISCOVERED = 3;
    public static final int HANDLER_MESSAGE_WHAT_DISCONNECT = 4;


    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private BeaconLeScanHandler mBeaconLeScanHandler;

    private static volatile BeaconModule INSTANCE;

    private Context mContext;

    private BeaconModule() {
    }

    public static BeaconModule getInstance() {
        if (INSTANCE == null) {
            synchronized (BeaconModule.class) {
                if (INSTANCE == null) {
                    INSTANCE = new BeaconModule();
                }
            }
        }
        return INSTANCE;
    }

    public void init(Context context) {
        LogModule.init(context);
        mContext = context;
        mHandler = new ServiceHandler(this);
        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    private ServiceHandler mHandler;

    public class ServiceHandler extends BaseHandler<BeaconModule> {
        private BeaconConnStateCallback connStateCallback;
        public ServiceHandler(BeaconModule module) {
            super(module);
        }

        @Override
        protected void handleMessage(BeaconModule module, Message msg) {
            switch (msg.what) {
                case HANDLER_MESSAGE_WHAT_CONNECTED:
                    mBluetoothGatt.discoverServices();
                    break;
                case HANDLER_MESSAGE_WHAT_DISCONNECTED:
                    disConnectBle();
                    connStateCallback.onDisConnected();
                    break;
                case HANDLER_MESSAGE_WHAT_SERVICES_DISCOVERED:
                    LogModule.i("连接成功！");
                    connStateCallback.onConnectSuccess();
                    break;
                case HANDLER_MESSAGE_WHAT_DISCONNECT:
                    if (mBluetoothGatt != null) {
                        if (refreshDeviceCache()) {
                            LogModule.i("清理GATT层蓝牙缓存");
                        }
                        LogModule.i("断开连接");
                        mBluetoothGatt.close();
                        mBluetoothGatt.disconnect();
                    }
                    break;
            }
        }

        public void setConnStateCallback(BeaconConnStateCallback connStateCallback) {
            this.connStateCallback = connStateCallback;
        }
    }

    public void setConnStateCallback(final BeaconConnStateCallback connStateCallback){
        mHandler.setConnStateCallback(connStateCallback);
    }

    /**
     * @Date 2017/12/12 0012
     * @Author wenzheng.liu
     * @Description 蓝牙是否打开
     */
    public boolean isBluetoothOpen() {
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 是否连接设备
     */
    public boolean isConnDevice(Context context, String address) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        int connState = bluetoothManager.getConnectionState(mBluetoothAdapter.getRemoteDevice(address), BluetoothProfile.GATT);
        return connState == BluetoothProfile.STATE_CONNECTED;
    }

    public void startScanDevice(ScanDeviceCallback scanDeviceCallback) {
        LogModule.i("开始扫描Beacon");
        mBeaconLeScanHandler = new BeaconLeScanHandler(scanDeviceCallback);
        mBluetoothAdapter.startLeScan(mBeaconLeScanHandler);
        scanDeviceCallback.onStartScan();
    }

    public void stopScanDevice() {
        if (mBeaconLeScanHandler != null) {
            LogModule.i("结束扫描Beacon");
            mBluetoothAdapter.stopLeScan(mBeaconLeScanHandler);
            mBeaconLeScanHandler = null;
        }
    }

    public void connDevice(final Context context, final String address, final BeaconConnStateCallback connStateCallback) {
        if (TextUtils.isEmpty(address)) {
            // TODO: 2017/12/12 0012 地址为空
            return;
        }
        if (!isBluetoothOpen()) {
            // TODO: 2017/12/12 0012 蓝牙未打开
            return;
        }
        if (isConnDevice(context, address)) {
            // TODO: 2017/12/12 0012 设备已连接
            return;
        }
        final BeaconConnStateHandler gattCallback = BeaconConnStateHandler.getInstance();
        gattCallback.setBeaconResponseHandler(new BeaconResponseHandler(context));
        setConnStateCallback(connStateCallback);
        gattCallback.setMessageHandler(mHandler);
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    LogModule.i("开始尝试连接");
                    mBluetoothGatt = (new BleConnectionCompat(context)).connectGatt(device, false, gattCallback);
                }
            });
        } else {
            LogModule.i("获取蓝牙设备失败");
        }
    }

    /**
     * @Date 2017/12/13 0013
     * @Author wenzheng.liu
     * @Description 断开连接
     */
    public void disConnectBle() {
        mHandler.sendEmptyMessage(BeaconModule.HANDLER_MESSAGE_WHAT_DISCONNECT);
    }

    /**
     * @Date 2017/12/13 0013
     * @Author wenzheng.liu
     * @Description Clears the internal cache and forces a refresh of the services from the
     * remote device.
     */
    private boolean refreshDeviceCache() {
        if (mBluetoothGatt != null) {
            try {
                BluetoothGatt localBluetoothGatt = mBluetoothGatt;
                Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
                if (localMethod != null) {
                    boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
                    return bool;
                }
            } catch (Exception localException) {
                LogModule.i("An exception occured while refreshing device");
            }
        }
        return false;
    }
}

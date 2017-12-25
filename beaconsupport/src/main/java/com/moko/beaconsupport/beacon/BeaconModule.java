package com.moko.beaconsupport.beacon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Message;
import android.text.TextUtils;

import com.moko.beaconsupport.callback.BeaconConnStateCallback;
import com.moko.beaconsupport.callback.BeaconResponseCallback;
import com.moko.beaconsupport.callback.OrderTaskCallback;
import com.moko.beaconsupport.callback.ScanDeviceCallback;
import com.moko.beaconsupport.entity.BeaconCharacteristic;
import com.moko.beaconsupport.entity.OrderType;
import com.moko.beaconsupport.handler.BaseHandler;
import com.moko.beaconsupport.handler.BeaconConnStateHandler;
import com.moko.beaconsupport.handler.BeaconLeScanHandler;
import com.moko.beaconsupport.handler.CharacteristicHandler;
import com.moko.beaconsupport.log.LogModule;
import com.moko.beaconsupport.task.OrderTask;
import com.moko.beaconsupport.utils.BleConnectionCompat;
import com.moko.beaconsupport.utils.Utils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Date 2017/12/7 0007
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beaconsupport.beacon.BeaconModule
 */
public class BeaconModule implements BeaconResponseCallback {
    public static final int HANDLER_MESSAGE_WHAT_CONNECTED = 1;
    public static final int HANDLER_MESSAGE_WHAT_DISCONNECTED = 2;
    public static final int HANDLER_MESSAGE_WHAT_SERVICES_DISCOVERED = 3;
    public static final int HANDLER_MESSAGE_WHAT_DISCONNECT = 4;
    public static final UUID DESCRIPTOR_UUID_NOTIFY = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private BeaconLeScanHandler mBeaconLeScanHandler;
    private HashMap<OrderType, BeaconCharacteristic> mBeaconCharacteristicMap;
    private BlockingQueue<OrderTask> mQueue;

    private static volatile BeaconModule INSTANCE;

    private Context mContext;

    private BeaconModule() {
        //no instance
        mQueue = new LinkedBlockingQueue<>();
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
                    mBeaconCharacteristicMap = CharacteristicHandler.getInstance().getCharacteristics(mBluetoothGatt);
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

    public void setConnStateCallback(final BeaconConnStateCallback connStateCallback) {
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
        gattCallback.setBeaconResponseCallback(this);
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

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////
    public void sendOrder(OrderTask... orderTasks) {
        if (orderTasks.length == 0) {
            return;
        }
        if (mQueue.isEmpty()) {
            for (OrderTask ordertask : orderTasks) {
                if (ordertask == null) {
                    continue;
                }
                mQueue.offer(ordertask);
            }
            if (!mQueue.isEmpty()) {
                executeTask(null);
            }
        } else {
            for (OrderTask ordertask : orderTasks) {
                if (ordertask == null) {
                    continue;
                }
                mQueue.offer(ordertask);
            }
        }
    }

    private void executeTask(OrderTaskCallback callback) {
        if (callback != null && mQueue.isEmpty()) {
            callback.onOrderFinish();
            return;
        }
        final OrderTask orderTask = mQueue.peek();
        if (mBluetoothGatt == null) {
            LogModule.i("executeTask : BluetoothGatt is null");
            return;
        }
        if (orderTask == null) {
            LogModule.i("executeTask : orderTask is null");
            return;
        }
        final BeaconCharacteristic beaconCharacteristic = mBeaconCharacteristicMap.get(orderTask.orderType);
        if (beaconCharacteristic == null) {
            LogModule.i("executeTask : beaconCharacteristic is null");
            return;
        }
        if (orderTask.responseType == OrderTask.RESPONSE_TYPE_READ) {
            sendReadOrder(orderTask, beaconCharacteristic);
        }
        if (orderTask.responseType == OrderTask.RESPONSE_TYPE_WRITE) {
            sendWriteOrder(orderTask, beaconCharacteristic);
        }
        if (orderTask.responseType == OrderTask.RESPONSE_TYPE_NOTIFY) {
            sendNotifyOrder(orderTask, beaconCharacteristic);
        }
        orderTimeoutHandler(orderTask);
    }

    // 发送可监听命令
    private void sendNotifyOrder(OrderTask orderTask, final BeaconCharacteristic beaconCharacteristic) {
        LogModule.i("set notification enable : " + orderTask.orderType.getName());
        final BluetoothGattDescriptor descriptor = beaconCharacteristic.characteristic.getDescriptor(DESCRIPTOR_UUID_NOTIFY);
        if (descriptor == null) {
            return;
        }
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBluetoothGatt.writeDescriptor(descriptor);
            }
        });
    }

    // 发送可写命令
    private void sendWriteOrder(OrderTask orderTask, final BeaconCharacteristic beaconCharacteristic) {
        LogModule.i("app to ibeacon write : " + orderTask.orderType.getName());
        LogModule.i(Utils.bytesToHexString(orderTask.assemble()));
        beaconCharacteristic.characteristic.setValue(orderTask.assemble());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBluetoothGatt.writeCharacteristic(beaconCharacteristic.characteristic);
            }
        });
    }

    // 发送可读命令
    private void sendReadOrder(OrderTask orderTask, final BeaconCharacteristic beaconCharacteristic) {
        LogModule.i("app to ibeacon read : " + orderTask.orderType.getName());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBluetoothGatt.readCharacteristic(beaconCharacteristic.characteristic);
            }
        });
    }

    private void orderTimeoutHandler(final OrderTask orderTask) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (orderTask.orderStatus != OrderTask.ORDER_STATUS_SUCCESS) {
                    LogModule.i("应答超时");
                    mQueue.poll();
                    orderTask.orderTaskCallback.onOrderTimeout(orderTask.orderType);
                    executeTask(orderTask.orderTaskCallback);
                }
            }
        }, 3000);
    }

    @Override
    public void onCharacteristicChanged(byte[] value) {
        if (mQueue.isEmpty()) {
            return;
        }
        OrderTask orderTask = mQueue.peek();
        if (value != null && value.length > 0) {
            switch (orderTask.orderType) {
                case runtimeAndChipModel:
                    if ("59".equals(Utils.byte2HexString(value[1]))) {
                        byte[] runtime = Arrays.copyOfRange(value, 2, value.length);
                        formatCommonOrder(orderTask, runtime);
                    }
                    if ("5b".equals(Utils.byte2HexString(value[1]).toLowerCase())) {
                        byte[] chipModel = Arrays.copyOfRange(value, 2, value.length);
                        formatCommonOrder(orderTask, chipModel);
                    }
                    break;
                case changePassword:
                    formatCommonOrder(orderTask, value);
                    break;
            }
        }
    }

    @Override
    public void onCharacteristicWrite(byte[] value) {
        if (mQueue.isEmpty()) {
            return;
        }
        OrderTask orderTask = mQueue.peek();
        if (value != null && value.length > 0) {
            switch (orderTask.orderType) {
                case overtime:
                case iBeaconUuid:
                case major:
                case minor:
                case measurePower:
                case transmission:
                case broadcastingInterval:
                case serialID:
                case iBeaconName:
                case connectionMode:
                    formatCommonOrder(orderTask, value);
                    break;
            }
        }
    }

    @Override
    public void onCharacteristicRead(byte[] value) {
        if (mQueue.isEmpty()) {
            return;
        }
        OrderTask orderTask = mQueue.peek();
        if (value != null && value.length > 0) {
            switch (orderTask.orderType) {
                case battery:
                case firmname:
                case devicename:
                case iBeaconDate:
                case hardwareVersion:
                case firmwareVersion:
                case iBeaconUuid:
                case major:
                case minor:
                case measurePower:
                case transmission:
                case broadcastingInterval:
                case serialID:
                case iBeaconName:
                case connectionMode:
                case softReboot:
                case iBeaconMac:
                case systemMark:
                case IEEEInfo:
                    formatCommonOrder(orderTask, value);
                    break;
            }
        }
    }

    @Override
    public void onDescriptorWrite() {
        if (mQueue.isEmpty()) {
            return;
        }
        OrderTask orderTask = mQueue.peek();
        LogModule.i("app to ibeacon notify : " + orderTask.orderType.getName());
        LogModule.i(Utils.bytesToHexString(orderTask.assemble()));
        final BeaconCharacteristic beaconCharacteristic = mBeaconCharacteristicMap.get(orderTask.orderType);
        beaconCharacteristic.characteristic.setValue(orderTask.assemble());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBluetoothGatt.writeCharacteristic(beaconCharacteristic.characteristic);
            }
        });
    }

    private void formatCommonOrder(OrderTask task, byte[] value) {
        task.orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        mQueue.poll();
        task.orderTaskCallback.onOrderResult(task.orderType, value);
        executeTask(task.orderTaskCallback);
    }

    public boolean isSyncData() {
        return mQueue != null && !mQueue.isEmpty();
    }
}

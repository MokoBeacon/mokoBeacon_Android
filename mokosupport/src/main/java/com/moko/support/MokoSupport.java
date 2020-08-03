package com.moko.support;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.callback.MokoResponseCallback;
import com.moko.support.callback.MokoScanDeviceCallback;
import com.moko.support.entity.MokoCharacteristic;
import com.moko.support.entity.OrderType;
import com.moko.support.handler.MokoCharacteristicHandler;
import com.moko.support.handler.MokoLeScanHandler;
import com.moko.support.log.LogModule;
import com.moko.support.task.OrderTask;
import com.moko.support.utils.MokoUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import no.nordicsemi.android.ble.BleManagerCallbacks;
import no.nordicsemi.android.ble.callback.SuccessCallback;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

/**
 * @Date 2017/12/7 0007
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.beacon.MokoSupport
 */
public class MokoSupport implements MokoResponseCallback {
    //    public static final int HANDLER_MESSAGE_WHAT_CONNECTED = 1;
//    public static final int HANDLER_MESSAGE_WHAT_DISCONNECTED = 2;
//    public static final int HANDLER_MESSAGE_WHAT_SERVICES_DISCOVERED = 3;
//    public static final int HANDLER_MESSAGE_WHAT_DISCONNECT = 4;
    public static final UUID DESCRIPTOR_UUID_NOTIFY = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private MokoLeScanHandler mMokoLeScanHandler;
    private HashMap<OrderType, MokoCharacteristic> mCharacteristicMap;
    private BlockingQueue<OrderTask> mQueue;
    private MokoScanDeviceCallback mMokoScanDeviceCallback;
//    private MokoConnStateCallback mMokoConnStateCallback;

    private static volatile MokoSupport INSTANCE;

    private Context mContext;

    private MokoBleManager mokoBleManager;

    private MokoSupport() {
        //no instance
        mQueue = new LinkedBlockingQueue<>();
    }

    public static MokoSupport getInstance() {
        if (INSTANCE == null) {
            synchronized (MokoSupport.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MokoSupport();
                }
            }
        }
        return INSTANCE;
    }

    public void init(Context context) {
        LogModule.init(context);
        mContext = context;
//        mHandler = new ServiceMessageHandler(this);
        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mHandler = new Handler(Looper.getMainLooper());
        mokoBleManager = MokoBleManager.getMokoBleManager(context);
        mokoBleManager.setBeaconResponseCallback(this);
        mokoBleManager.setGattCallbacks(new BleManagerCallbacks() {
            @Override
            public void onDeviceConnecting(@NonNull BluetoothDevice device) {

            }

            @Override
            public void onDeviceConnected(@NonNull BluetoothDevice device) {
            }

            @Override
            public void onDeviceDisconnecting(@NonNull BluetoothDevice device) {
                if (mQueue != null && !mQueue.isEmpty()) {
                    mQueue.clear();
                }
            }

            @Override
            public void onDeviceDisconnected(@NonNull BluetoothDevice device) {
                Intent intent = new Intent(MokoConstants.ACTION_CONNECT_DISCONNECTED);
                mContext.sendOrderedBroadcast(intent, null);
            }

            @Override
            public void onLinkLossOccurred(@NonNull BluetoothDevice device) {

            }

            @Override
            public void onServicesDiscovered(@NonNull BluetoothDevice device, boolean optionalServicesFound) {

            }

            @Override
            public void onDeviceReady(@NonNull BluetoothDevice device) {

            }

            @Override
            public void onBondingRequired(@NonNull BluetoothDevice device) {

            }

            @Override
            public void onBonded(@NonNull BluetoothDevice device) {

            }

            @Override
            public void onBondingFailed(@NonNull BluetoothDevice device) {

            }

            @Override
            public void onError(@NonNull BluetoothDevice device, @NonNull String message, int errorCode) {

            }

            @Override
            public void onDeviceNotSupported(@NonNull BluetoothDevice device) {

            }
        });
    }

    private Handler mHandler;

//    public class ServiceMessageHandler extends BaseMessageHandler<MokoSupport> {
////        private MokoConnStateCallback mokoConnStateCallback;
//
//        public ServiceMessageHandler(MokoSupport module) {
//            super(module);
//        }
//
//        @Override
//        protected void handleMessage(MokoSupport module, Message msg) {
//            switch (msg.what) {
//                case HANDLER_MESSAGE_WHAT_CONNECTED:
//                    mBluetoothGatt.discoverServices();
//                    break;
//                case HANDLER_MESSAGE_WHAT_DISCONNECTED:
//                    disConnectBle();
////                    mokoConnStateCallback.onDisConnected();
//                    break;
//                case HANDLER_MESSAGE_WHAT_SERVICES_DISCOVERED:
//                    LogModule.i("连接成功！");
//                    mCharacteristicMap = MokoCharacteristicHandler.getInstance().getCharacteristics(mBluetoothGatt);
////                    mokoConnStateCallback.onConnectSuccess();
//                    break;
//                case HANDLER_MESSAGE_WHAT_DISCONNECT:
//                    if (mQueue != null && !mQueue.isEmpty()) {
//                        mQueue.clear();
//                    }
//                    if (mBluetoothGatt != null) {
//                        if (refreshDeviceCache()) {
//                            LogModule.i("清理GATT层蓝牙缓存");
//                        }
//                        LogModule.i("断开连接");
//                        mBluetoothGatt.close();
//                        mBluetoothGatt.disconnect();
//                    }
//                    break;
//            }
//        }
//
////        public void setMokoConnStateCallback(MokoConnStateCallback mokoConnStateCallback) {
////            this.mokoConnStateCallback = mokoConnStateCallback;
////        }
//    }

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

    public void startScanDevice(MokoScanDeviceCallback mokoScanDeviceCallback) {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            LogModule.i("开始扫描Beacon");
        }
        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        List<ScanFilter> filters = Collections.singletonList(new ScanFilter.Builder().build());
        mMokoLeScanHandler = new MokoLeScanHandler(mokoScanDeviceCallback);
        scanner.startScan(filters, settings, mMokoLeScanHandler);
        mMokoScanDeviceCallback = mokoScanDeviceCallback;
        mokoScanDeviceCallback.onStartScan();
    }

    public void stopScanDevice() {
        if (mMokoLeScanHandler != null && mMokoScanDeviceCallback != null) {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                LogModule.i("结束扫描Beacon");
            }
            final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            scanner.stopScan(mMokoLeScanHandler);
            mMokoScanDeviceCallback.onStopScan();
            mMokoLeScanHandler = null;
            mMokoScanDeviceCallback = null;
        }
    }

    public void connDevice(final Context context, final String address) {
        if (TextUtils.isEmpty(address)) {
            LogModule.i("connDevice: 地址为空");
            return;
        }
        if (!isBluetoothOpen()) {
            LogModule.i("connDevice: 蓝牙未打开");
            return;
        }
        if (isConnDevice(context, address)) {
            LogModule.i("connDevice: 设备已连接");
            return;
        }
//        final MokoConnStateHandler gattCallback = MokoConnStateHandler.getInstance();
//        gattCallback.setBeaconResponseCallback(this);
//        mMokoConnStateCallback = mokoConnStateCallback;
//        mHandler.setMokoConnStateCallback(mokoConnStateCallback);
//        gattCallback.setMessageHandler(mHandler);
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    LogModule.i("开始尝试连接");
//                    mBluetoothGatt = (new BleConnectionCompat(context)).connectGatt(device, false, gattCallback);
                    mokoBleManager.connect(device)
                            .retry(3, 100)
                            .enqueue();
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
//        mHandler.sendEmptyMessage(MokoSupport.HANDLER_MESSAGE_WHAT_DISCONNECT);
        mokoBleManager.disconnect().enqueue();
    }

    /**
     * @Date 2017/12/13 0013
     * @Author wenzheng.liu
     * @Description Clears the internal cache and forces a refresh of the services from the
     * remote device.
     */
//    private boolean refreshDeviceCache() {
//        if (mBluetoothGatt != null) {
//            try {
//                BluetoothGatt localBluetoothGatt = mBluetoothGatt;
//                Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
//                if (localMethod != null) {
//                    boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
//                    return bool;
//                }
//            } catch (Exception localException) {
//                LogModule.i("An exception occured while refreshing device");
//            }
//        }
//        return false;
//    }

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
            executeTask(null);
        } else {
            for (OrderTask ordertask : orderTasks) {
                if (ordertask == null) {
                    continue;
                }
                mQueue.offer(ordertask);
            }
        }
    }

    private void executeTask(MokoOrderTaskCallback callback) {
        if (callback != null && mQueue.isEmpty()) {
            callback.onOrderFinish();
            return;
        }
        final OrderTask orderTask = mQueue.peek();
        if (mBluetoothGatt == null) {
            mQueue.clear();
            LogModule.i("executeTask : BluetoothGatt is null");
            return;
        }
        if (orderTask == null) {
            LogModule.i("executeTask : orderTask is null");
            return;
        }
        final MokoCharacteristic mokoCharacteristic = mCharacteristicMap.get(orderTask.orderType);
        if (mokoCharacteristic == null) {
            mQueue.clear();
            disConnectBle();
//            mMokoConnStateCallback.onDisConnected();
            LogModule.i("executeTask : mokoCharacteristic is null");
            return;
        }
        if (orderTask.responseType == OrderTask.RESPONSE_TYPE_READ) {
            sendReadOrder(orderTask, mokoCharacteristic);
        }
        if (orderTask.responseType == OrderTask.RESPONSE_TYPE_WRITE) {
            sendWriteOrder(orderTask, mokoCharacteristic);
        }
        if (orderTask.responseType == OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE) {
            sendWriteNoResponseOrder(orderTask, mokoCharacteristic);
        }
        if (orderTask.responseType == OrderTask.RESPONSE_TYPE_NOTIFY) {
            sendNotifyOrder(orderTask, mokoCharacteristic);
        }
        orderTimeoutHandler(orderTask);
    }

    // 发送可监听命令
    private void sendNotifyOrder(OrderTask orderTask, final MokoCharacteristic mokoCharacteristic) {
        LogModule.i("app set device notify : " + orderTask.orderType.getName());
        final BluetoothGattDescriptor descriptor = mokoCharacteristic.characteristic.getDescriptor(DESCRIPTOR_UUID_NOTIFY);
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
    private void sendWriteOrder(OrderTask orderTask, final MokoCharacteristic mokoCharacteristic) {
        LogModule.i("app to device write : " + orderTask.orderType.getName());
        LogModule.i(MokoUtils.bytesToHexString(orderTask.assemble()));
        mokoCharacteristic.characteristic.setValue(orderTask.assemble());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBluetoothGatt.writeCharacteristic(mokoCharacteristic.characteristic);
            }
        });
    }

    // 发送可写无应答命令
    private void sendWriteNoResponseOrder(OrderTask orderTask, final MokoCharacteristic mokoCharacteristic) {
        LogModule.i("app to device write no response : " + orderTask.orderType.getName());
        LogModule.i(MokoUtils.bytesToHexString(orderTask.assemble()));
        mokoCharacteristic.characteristic.setValue(orderTask.assemble());
        mokoCharacteristic.characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBluetoothGatt.writeCharacteristic(mokoCharacteristic.characteristic);
            }
        });
    }

    // 发送可读命令
    private void sendReadOrder(OrderTask orderTask, final MokoCharacteristic mokoCharacteristic) {
        LogModule.i("app to device read : " + orderTask.orderType.getName());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBluetoothGatt.readCharacteristic(mokoCharacteristic.characteristic);
            }
        });
    }

    // 直接发送命令(升级专用)
    public void sendDirectOrder(OrderTask orderTask) {
        final MokoCharacteristic mokoCharacteristic = mCharacteristicMap.get(orderTask.orderType);
        if (mokoCharacteristic == null) {
            LogModule.i("executeTask : mokoCharacteristic is null");
            return;
        }
        LogModule.i("app to device write no response : " + orderTask.orderType.getName());
        LogModule.i(MokoUtils.bytesToHexString(orderTask.assemble()));
        mokoCharacteristic.characteristic.setValue(orderTask.assemble());
        mokoCharacteristic.characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBluetoothGatt.writeCharacteristic(mokoCharacteristic.characteristic);
            }
        });
    }

    private void orderTimeoutHandler(final OrderTask orderTask) {
        long delayTime = 3000;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (orderTask.orderStatus != OrderTask.ORDER_STATUS_SUCCESS) {
                    LogModule.i("应答超时");
                    mQueue.poll();
                    orderTask.mokoOrderTaskCallback.onOrderTimeout(orderTask.orderType);
                    executeTask(orderTask.mokoOrderTaskCallback);
                }
            }
        }, delayTime);
    }

    @Override
    public void onCharacteristicChanged(BluetoothGattCharacteristic characteristic, byte[] value) {
        if (!mQueue.isEmpty()) {
            // 非延时应答
            OrderTask orderTask = mQueue.peek();
            if (value != null && value.length > 0) {
                switch (orderTask.orderType) {
                    case writeAndNotify:
                    case changePassword:
                        formatCommonOrder(orderTask, value);
                        break;
                }
            }
        } else {
            OrderType orderType = null;
            // 延时应答
            if (characteristic.getUuid().toString().equals(OrderType.writeAndNotify.getUuid())) {
                // 写通知命令
                orderType = OrderType.writeAndNotify;
            }

            if (orderType != null) {
                LogModule.i(orderType.getName());
                Intent intent = new Intent(MokoConstants.ACTION_RESPONSE_NOTIFY);
                intent.putExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TYPE, orderType);
                intent.putExtra(MokoConstants.EXTRA_KEY_RESPONSE_VALUE, value);
                mContext.sendOrderedBroadcast(intent, null);
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
                case softVersion:
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
        LogModule.i("device to app notify : " + orderTask.orderType.getName());
        orderTask.orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        mQueue.poll();
        executeTask(orderTask.mokoOrderTaskCallback);
    }

    @Override
    public void onBatteryValueReceived(BluetoothGatt gatt) {
        mBluetoothGatt = gatt;
        mCharacteristicMap = MokoCharacteristicHandler.getInstance().getCharacteristics(gatt);
        Intent intent = new Intent(MokoConstants.ACTION_CONNECT_SUCCESS);
        mContext.sendOrderedBroadcast(intent, null);
    }

    private void formatCommonOrder(OrderTask task, byte[] value) {
        task.orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        mQueue.poll();
        task.mokoOrderTaskCallback.onOrderResult(task.orderType, value, task.responseType);
        executeTask(task.mokoOrderTaskCallback);
    }

    public boolean isSyncData() {
        return mQueue != null && !mQueue.isEmpty();
    }
}

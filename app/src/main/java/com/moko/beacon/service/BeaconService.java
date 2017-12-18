package com.moko.beacon.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;

import com.moko.beacon.BeaconConstants;
import com.moko.beacon.base.BaseHandler;
import com.moko.beaconsupport.beacon.BeaconModule;
import com.moko.beaconsupport.callback.BeaconConnStateCallback;
import com.moko.beaconsupport.callback.OrderTaskCallback;
import com.moko.beaconsupport.callback.ScanDeviceCallback;
import com.moko.beaconsupport.entity.OrderType;
import com.moko.beaconsupport.log.LogModule;
import com.moko.beaconsupport.task.BatteryTask;
import com.moko.beaconsupport.task.BroadcastingIntervalTask;
import com.moko.beaconsupport.task.ChangePasswordTask;
import com.moko.beaconsupport.task.ChipModelTask;
import com.moko.beaconsupport.task.ConnectionModeTask;
import com.moko.beaconsupport.task.DevicenameTask;
import com.moko.beaconsupport.task.FirmnameTask;
import com.moko.beaconsupport.task.FirmwareVersionTask;
import com.moko.beaconsupport.task.HardwareVersionTask;
import com.moko.beaconsupport.task.IBeaconDateTask;
import com.moko.beaconsupport.task.IBeaconMacTask;
import com.moko.beaconsupport.task.IBeaconNameTask;
import com.moko.beaconsupport.task.IBeaconUuidTask;
import com.moko.beaconsupport.task.IEEEInfoTask;
import com.moko.beaconsupport.task.MajorTask;
import com.moko.beaconsupport.task.MeasurePowerTask;
import com.moko.beaconsupport.task.MinorTask;
import com.moko.beaconsupport.task.OrderTask;
import com.moko.beaconsupport.task.OvertimeTask;
import com.moko.beaconsupport.task.RunntimeTask;
import com.moko.beaconsupport.task.SerialIDTask;
import com.moko.beaconsupport.task.SoftRebootModeTask;
import com.moko.beaconsupport.task.SystemMarkTask;
import com.moko.beaconsupport.task.TransmissionTask;

/**
 * @Date 2017/12/7 0007
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beacon.service.BeaconService
 */
public class BeaconService extends Service implements BeaconConnStateCallback, OrderTaskCallback {
    private IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public BeaconService getService() {
            return BeaconService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogModule.i("启动后台服务");
        mHandler = new ServiceHandler(this);

    }

    @Override
    public void onDestroy() {
        LogModule.i("关闭后台服务");
        BeaconModule.getInstance().disConnectBle();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    ///////////////////////////////////////////////////////////////////////////
    // 处理扫描
    ///////////////////////////////////////////////////////////////////////////

    public void startScanDevice(ScanDeviceCallback callback) {
        BeaconModule.getInstance().startScanDevice(callback);
    }

    public void stopScanDevice() {
        BeaconModule.getInstance().stopScanDevice();
    }

    ///////////////////////////////////////////////////////////////////////////
    // 处理连接
    ///////////////////////////////////////////////////////////////////////////

    public void connDevice(String address) {
        BeaconModule.getInstance().connDevice(this, address, this);
    }

    @Override
    public void onConnectSuccess() {
        Intent intent = new Intent(BeaconConstants.ACTION_CONNECT_SUCCESS);
        sendOrderedBroadcast(intent, null);
    }

    @Override
    public void onDisConnected() {
        Intent intent = new Intent(BeaconConstants.ACTION_CONNECT_DISCONNECTED);
        sendOrderedBroadcast(intent, null);
    }

    ///////////////////////////////////////////////////////////////////////////
    // 处理应答
    ///////////////////////////////////////////////////////////////////////////

    /**
     * @Date 2017/12/14 0014
     * @Author wenzheng.liu
     * @Description 获取可读信息
     */
    public void getReadableData(String password) {
        BeaconModule.getInstance().sendOrder(getBattery(), getFirmname(), getDevicename(), getiBeaconDate(),
                getHardwareVersion(), getFirmwareVersion(), getSystemMark(), getIEEEInfo(), getIBeaconUuid(),
                getMajor(), getMinor(), getMeasurePower(), getTransmission(), getBroadcastingInterval(),
                getSerialID(), getIBeaconName(), getConnectionMode(), getIBeaconMac(),
                getRunntime(), getChipModel(),
                setOvertime(), setChangePassword(password));
    }

    public void sendOrder(OrderTask... orderTasks) {
        BeaconModule.getInstance().sendOrder(orderTasks);
    }

    public OrderTask getBattery() {
        BatteryTask batteryTask = new BatteryTask(this, OrderTask.RESPONSE_TYPE_READ);
        return batteryTask;
    }

    public OrderTask getFirmname() {
        FirmnameTask firmnameTask = new FirmnameTask(this, OrderTask.RESPONSE_TYPE_READ);
        return firmnameTask;
    }

    public OrderTask getDevicename() {
        DevicenameTask devicenameTask = new DevicenameTask(this, OrderTask.RESPONSE_TYPE_READ);
        return devicenameTask;
    }

    public OrderTask getiBeaconDate() {
        IBeaconDateTask iBeaconDateTask = new IBeaconDateTask(this, OrderTask.RESPONSE_TYPE_READ);
        return iBeaconDateTask;
    }

    public OrderTask getHardwareVersion() {
        HardwareVersionTask hardwareVersionTask = new HardwareVersionTask(this, OrderTask.RESPONSE_TYPE_READ);
        return hardwareVersionTask;
    }

    public OrderTask getFirmwareVersion() {
        FirmwareVersionTask firmwareVersionTask = new FirmwareVersionTask(this, OrderTask.RESPONSE_TYPE_READ);
        return firmwareVersionTask;
    }

    public OrderTask getSystemMark() {
        SystemMarkTask systemMarkTask = new SystemMarkTask(this, OrderTask.RESPONSE_TYPE_READ);
        return systemMarkTask;
    }

    public OrderTask getIEEEInfo() {
        IEEEInfoTask ieeeInfoTask = new IEEEInfoTask(this, OrderTask.RESPONSE_TYPE_READ);
        return ieeeInfoTask;
    }

    public OrderTask getRunntime() {
        RunntimeTask runntimeTask = new RunntimeTask(this, OrderTask.RESPONSE_TYPE_NOTIFY);
        return runntimeTask;
    }

    public OrderTask getChipModel() {
        ChipModelTask chipModelTask = new ChipModelTask(this, OrderTask.RESPONSE_TYPE_NOTIFY);
        return chipModelTask;
    }

    public OrderTask setChangePassword(String password) {
        ChangePasswordTask changePasswordTask = new ChangePasswordTask(this, OrderTask.RESPONSE_TYPE_NOTIFY);
        changePasswordTask.setData(password);
        return changePasswordTask;
    }

    public OrderTask getIBeaconUuid() {
        IBeaconUuidTask iBeaconUuidTask = new IBeaconUuidTask(this, OrderTask.RESPONSE_TYPE_READ);
        return iBeaconUuidTask;
    }

    public OrderTask setIBeaconUuid(String uuid) {
        IBeaconUuidTask iBeaconUuidTask = new IBeaconUuidTask(this, OrderTask.RESPONSE_TYPE_WRITE);
        iBeaconUuidTask.setData(uuid);
        return iBeaconUuidTask;
    }

    public OrderTask getMajor() {
        MajorTask majorTask = new MajorTask(this, OrderTask.RESPONSE_TYPE_READ);
        return majorTask;
    }

    public OrderTask setMajor(int major) {
        MajorTask majorTask = new MajorTask(this, OrderTask.RESPONSE_TYPE_WRITE);
        majorTask.setData(major);
        return majorTask;
    }

    public OrderTask getMinor() {
        MinorTask minorTask = new MinorTask(this, OrderTask.RESPONSE_TYPE_READ);
        return minorTask;
    }

    public OrderTask setMinor(int minor) {
        MinorTask minorTask = new MinorTask(this, OrderTask.RESPONSE_TYPE_WRITE);
        minorTask.setData(minor);
        return minorTask;
    }

    public OrderTask getMeasurePower() {
        MeasurePowerTask measurePowerTask = new MeasurePowerTask(this, OrderTask.RESPONSE_TYPE_READ);
        return measurePowerTask;
    }

    public OrderTask setMeasurePower(int measurePower) {
        MeasurePowerTask measurePowerTask = new MeasurePowerTask(this, OrderTask.RESPONSE_TYPE_WRITE);
        measurePowerTask.setData(measurePower);
        return measurePowerTask;
    }


    public OrderTask getTransmission() {
        TransmissionTask transmissionTask = new TransmissionTask(this, OrderTask.RESPONSE_TYPE_READ);
        return transmissionTask;
    }

    public OrderTask getBroadcastingInterval() {
        BroadcastingIntervalTask broadcastingIntervalTask = new BroadcastingIntervalTask(this, OrderTask.RESPONSE_TYPE_READ);
        return broadcastingIntervalTask;
    }

    public OrderTask getSerialID() {
        SerialIDTask serialIDTask = new SerialIDTask(this, OrderTask.RESPONSE_TYPE_READ);
        return serialIDTask;
    }

    public OrderTask getIBeaconName() {
        IBeaconNameTask iBeaconNameTask = new IBeaconNameTask(this, OrderTask.RESPONSE_TYPE_READ);
        return iBeaconNameTask;
    }

    public OrderTask getConnectionMode() {
        ConnectionModeTask connectionModeTask = new ConnectionModeTask(this, OrderTask.RESPONSE_TYPE_READ);
        return connectionModeTask;
    }

    public OrderTask getSoftReboot() {
        SoftRebootModeTask softRebootModeTask = new SoftRebootModeTask(this, OrderTask.RESPONSE_TYPE_READ);
        return softRebootModeTask;
    }

    public OrderTask getIBeaconMac() {
        IBeaconMacTask iBeaconMacTask = new IBeaconMacTask(this, OrderTask.RESPONSE_TYPE_READ);
        return iBeaconMacTask;
    }

    public OrderTask setOvertime() {
        OvertimeTask overtimeTask = new OvertimeTask(this, OrderTask.RESPONSE_TYPE_WRITE);
        return overtimeTask;
    }

    @Override
    public void onOrderResult(OrderType orderType, byte[] value) {
        Intent intent = new Intent(BeaconConstants.ACTION_RESPONSE_SUCCESS);
        intent.putExtra(BeaconConstants.EXTRA_KEY_RESPONSE_ORDER_TYPE, orderType);
        intent.putExtra(BeaconConstants.EXTRA_KEY_RESPONSE_VALUE, value);
        sendOrderedBroadcast(intent, null);
    }

    @Override
    public void onOrderTimeout(OrderType orderType) {
        Intent intent = new Intent(BeaconConstants.ACTION_RESPONSE_TIMEOUT);
        intent.putExtra(BeaconConstants.EXTRA_KEY_RESPONSE_ORDER_TYPE, orderType);
        sendOrderedBroadcast(intent, null);
    }

    @Override
    public void onOrderFinish() {
        LogModule.i("任务完成");
        Intent intent = new Intent(BeaconConstants.ACTION_RESPONSE_FINISH);
        sendOrderedBroadcast(intent, null);
    }

    public ServiceHandler mHandler;

    public class ServiceHandler extends BaseHandler<BeaconService> {

        public ServiceHandler(BeaconService service) {
            super(service);
        }

        @Override
        protected void handleMessage(BeaconService service, Message msg) {
        }
    }
}

package com.moko.beacon.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;

import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.callback.MokoConnStateCallback;
import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.callback.MokoScanDeviceCallback;
import com.moko.support.entity.OrderType;
import com.moko.support.handler.BaseMessageHandler;
import com.moko.support.log.LogModule;
import com.moko.support.task.BatteryTask;
import com.moko.support.task.BroadcastingIntervalTask;
import com.moko.support.task.ChangePasswordTask;
import com.moko.support.task.ChipModelTask;
import com.moko.support.task.ConnectionModeTask;
import com.moko.support.task.DevicenameTask;
import com.moko.support.task.FirmnameTask;
import com.moko.support.task.FirmwareVersionTask;
import com.moko.support.task.HardwareVersionTask;
import com.moko.support.task.IBeaconDateTask;
import com.moko.support.task.IBeaconMacTask;
import com.moko.support.task.IBeaconNameTask;
import com.moko.support.task.IBeaconUuidTask;
import com.moko.support.task.IEEEInfoTask;
import com.moko.support.task.MajorTask;
import com.moko.support.task.MeasurePowerTask;
import com.moko.support.task.MinorTask;
import com.moko.support.task.NotifyTask;
import com.moko.support.task.OrderTask;
import com.moko.support.task.OvertimeTask;
import com.moko.support.task.RunntimeTask;
import com.moko.support.task.SerialIDTask;
import com.moko.support.task.SoftRebootModeTask;
import com.moko.support.task.SystemMarkTask;
import com.moko.support.task.ThreeAxesTask;
import com.moko.support.task.TransmissionTask;

/**
 * @Date 2017/12/7 0007
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beacon.service.BeaconService
 */
public class BeaconService extends Service implements MokoConnStateCallback, MokoOrderTaskCallback {
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
        MokoSupport.getInstance().disConnectBle();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    ///////////////////////////////////////////////////////////////////////////
    // 处理扫描
    ///////////////////////////////////////////////////////////////////////////

    public void startScanDevice(MokoScanDeviceCallback callback) {
        MokoSupport.getInstance().startScanDevice(callback);
    }

    public void stopScanDevice() {
        MokoSupport.getInstance().stopScanDevice();
    }

    ///////////////////////////////////////////////////////////////////////////
    // 处理连接
    ///////////////////////////////////////////////////////////////////////////

    public void connDevice(String address) {
        MokoSupport.getInstance().connDevice(this, address, this);
    }

    @Override
    public void onConnectSuccess() {
        Intent intent = new Intent(MokoConstants.ACTION_CONNECT_SUCCESS);
        sendOrderedBroadcast(intent, null);
    }

    @Override
    public void onDisConnected() {
        Intent intent = new Intent(MokoConstants.ACTION_CONNECT_DISCONNECTED);
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
        MokoSupport.getInstance().sendOrder(getBattery(), getFirmname(), getDevicename(), getiBeaconDate(),
                getHardwareVersion(), getFirmwareVersion(), getSystemMark(), getIEEEInfo(), getIBeaconUuid(),
                getMajor(), getMinor(), getMeasurePower(), getTransmission(), getBroadcastingInterval(),
                getSerialID(), getIBeaconName(), getConnectionMode(), getIBeaconMac(),
                setNotify(),
                getRunntime(), getChipModel(),
                setOvertime(),
                setChangePasswordNotify(),
                setChangePassword(password));
    }

    public void sendOrder(OrderTask... orderTasks) {
        MokoSupport.getInstance().sendOrder(orderTasks);
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

    public OrderTask setNotify() {
        NotifyTask notifyTask = new NotifyTask(this, OrderTask.RESPONSE_TYPE_NOTIFY);
        return notifyTask;
    }

    public OrderTask setChangePasswordNotify() {
        ChangePasswordTask changePasswordTask = new ChangePasswordTask(this, OrderTask.RESPONSE_TYPE_NOTIFY);
        return changePasswordTask;
    }

    public OrderTask getRunntime() {
        RunntimeTask runntimeTask = new RunntimeTask(this, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        return runntimeTask;
    }

    public OrderTask getChipModel() {
        ChipModelTask chipModelTask = new ChipModelTask(this, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        return chipModelTask;
    }

    public OrderTask setThreeAxes(boolean isOpen) {
        ThreeAxesTask threeAxesTask = new ThreeAxesTask(this, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        threeAxesTask.setData(isOpen);
        return threeAxesTask;
    }

    public OrderTask setChangePassword(String password) {
        ChangePasswordTask changePasswordTask = new ChangePasswordTask(this, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
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

    public OrderTask setTransmission(int transmission) {
        TransmissionTask transmissionTask = new TransmissionTask(this, OrderTask.RESPONSE_TYPE_WRITE);
        transmissionTask.setData(transmission);
        return transmissionTask;
    }

    public OrderTask getBroadcastingInterval() {
        BroadcastingIntervalTask broadcastingIntervalTask = new BroadcastingIntervalTask(this, OrderTask.RESPONSE_TYPE_READ);
        return broadcastingIntervalTask;
    }

    public OrderTask setBroadcastingInterval(int broadcastInterval) {
        BroadcastingIntervalTask broadcastingIntervalTask = new BroadcastingIntervalTask(this, OrderTask.RESPONSE_TYPE_WRITE);
        broadcastingIntervalTask.setData(broadcastInterval);
        return broadcastingIntervalTask;
    }

    public OrderTask getSerialID() {
        SerialIDTask serialIDTask = new SerialIDTask(this, OrderTask.RESPONSE_TYPE_READ);
        return serialIDTask;
    }

    public OrderTask setSerialID(String deviceId) {
        SerialIDTask serialIDTask = new SerialIDTask(this, OrderTask.RESPONSE_TYPE_WRITE);
        serialIDTask.setData(deviceId);
        return serialIDTask;
    }

    public OrderTask getIBeaconName() {
        IBeaconNameTask iBeaconNameTask = new IBeaconNameTask(this, OrderTask.RESPONSE_TYPE_READ);
        return iBeaconNameTask;
    }

    public OrderTask setIBeaconName(String deviceName) {
        IBeaconNameTask iBeaconNameTask = new IBeaconNameTask(this, OrderTask.RESPONSE_TYPE_WRITE);
        iBeaconNameTask.setData(deviceName);
        return iBeaconNameTask;
    }

    public OrderTask getConnectionMode() {
        ConnectionModeTask connectionModeTask = new ConnectionModeTask(this, OrderTask.RESPONSE_TYPE_READ);
        return connectionModeTask;
    }

    public OrderTask setConnectionMode(String connectionMode) {
        ConnectionModeTask connectionModeTask = new ConnectionModeTask(this, OrderTask.RESPONSE_TYPE_WRITE);
        connectionModeTask.setData(connectionMode);
        return connectionModeTask;
    }

    public OrderTask setSoftReboot() {
        SoftRebootModeTask softRebootModeTask = new SoftRebootModeTask(this, OrderTask.RESPONSE_TYPE_WRITE);
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
        Intent intent = new Intent(MokoConstants.ACTION_RESPONSE_SUCCESS);
        intent.putExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TYPE, orderType);
        intent.putExtra(MokoConstants.EXTRA_KEY_RESPONSE_VALUE, value);
        sendOrderedBroadcast(intent, null);
    }

    @Override
    public void onOrderTimeout(OrderType orderType) {
        Intent intent = new Intent(MokoConstants.ACTION_RESPONSE_TIMEOUT);
        intent.putExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TYPE, orderType);
        sendOrderedBroadcast(intent, null);
    }

    @Override
    public void onOrderFinish() {
        LogModule.i("任务完成");
        Intent intent = new Intent(MokoConstants.ACTION_RESPONSE_FINISH);
        sendOrderedBroadcast(intent, null);
    }

    public ServiceHandler mHandler;

    public class ServiceHandler extends BaseMessageHandler<BeaconService> {

        public ServiceHandler(BeaconService service) {
            super(service);
        }

        @Override
        protected void handleMessage(BeaconService service, Message msg) {
        }
    }
}

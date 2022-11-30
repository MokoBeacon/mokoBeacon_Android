package com.moko.support;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.entity.ParamsKeyEnum;
import com.moko.support.task.GetAdvIntervalTask;
import com.moko.support.task.GetAdvNameTask;
import com.moko.support.task.GetBatteryTask;
import com.moko.support.task.GetConnectionTask;
import com.moko.support.task.GetDeviceMacTask;
import com.moko.support.task.GetDeviceUUIDTask;
import com.moko.support.task.GetFirmwareRevisionTask;
import com.moko.support.task.GetHardwareRevisionTask;
import com.moko.support.task.GetMajorTask;
import com.moko.support.task.GetManufacturerNameTask;
import com.moko.support.task.GetMeasurePowerTask;
import com.moko.support.task.GetMinorTask;
import com.moko.support.task.GetModelNumberTask;
import com.moko.support.task.GetSerialIDTask;
import com.moko.support.task.GetSerialNumberTask;
import com.moko.support.task.GetSoftwareRevisionTask;
import com.moko.support.task.GetTransmissionTask;
import com.moko.support.task.ParamsReadTask;
import com.moko.support.task.ParamsWriteTask;
import com.moko.support.task.SetAdvIntervalTask;
import com.moko.support.task.SetAdvNameTask;
import com.moko.support.task.SetConnectionTask;
import com.moko.support.task.SetDeviceUUIDTask;
import com.moko.support.task.SetMajorTask;
import com.moko.support.task.SetMeasurePowerTask;
import com.moko.support.task.SetMinorTask;
import com.moko.support.task.SetOvertimeTask;
import com.moko.support.task.SetPasswordTask;
import com.moko.support.task.SetSerialIDTask;
import com.moko.support.task.SetSoftRebootTask;
import com.moko.support.task.SetTransmissionTask;

public class OrderTaskAssembler {
    ///////////////////////////////////////////////////////////////////////////
    // READ
    ///////////////////////////////////////////////////////////////////////////

    public static OrderTask getBattery() {
        GetBatteryTask task = new GetBatteryTask();
        return task;
    }

    public static OrderTask getAdvInterval() {
        GetAdvIntervalTask task = new GetAdvIntervalTask();
        return task;
    }

    public static OrderTask getChipModel() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.GET_CHIP_MODEL);
        return task;
    }

    public static OrderTask getConnection() {
        GetConnectionTask task = new GetConnectionTask();
        return task;
    }

    public static OrderTask getDeviceModel() {
        GetModelNumberTask task = new GetModelNumberTask();
        return task;
    }

    public static OrderTask getManufacturer() {
        GetManufacturerNameTask task = new GetManufacturerNameTask();
        return task;
    }

    public static OrderTask getFirmwareVersion() {
        GetFirmwareRevisionTask task = new GetFirmwareRevisionTask();
        return task;
    }

    public static OrderTask getHardwareVersion() {
        GetHardwareRevisionTask task = new GetHardwareRevisionTask();
        return task;
    }

    public static OrderTask getProductDate() {
        GetSerialNumberTask task = new GetSerialNumberTask();
        return task;
    }

    public static OrderTask getDeviceMac() {
        GetDeviceMacTask task = new GetDeviceMacTask();
        return task;
    }

    public static OrderTask getAdvName() {
        GetAdvNameTask task = new GetAdvNameTask();
        return task;
    }

    public static OrderTask getDeviceUUID() {
        GetDeviceUUIDTask task = new GetDeviceUUIDTask();
        return task;
    }

    public static OrderTask getMajor() {
        GetMajorTask task = new GetMajorTask();
        return task;
    }

    public static OrderTask getMinor() {
        GetMinorTask task = new GetMinorTask();
        return task;
    }

    public static OrderTask getMeasurePower() {
        GetMeasurePowerTask task = new GetMeasurePowerTask();
        return task;
    }

    public static OrderTask getRuntime() {
        ParamsReadTask task = new ParamsReadTask();
        task.setData(ParamsKeyEnum.GET_RUNTIME);
        return task;
    }

    public static OrderTask getTransmission() {
        GetTransmissionTask task = new GetTransmissionTask();
        return task;
    }

    public static OrderTask getSoftVersion() {
        GetSoftwareRevisionTask task = new GetSoftwareRevisionTask();
        return task;
    }

    public static OrderTask getSerialID() {
        GetSerialIDTask task = new GetSerialIDTask();
        return task;
    }


    ///////////////////////////////////////////////////////////////////////////
    // WRITE
    ///////////////////////////////////////////////////////////////////////////

    public static OrderTask setAdvInterval(int advInterval) {
        SetAdvIntervalTask task = new SetAdvIntervalTask();
        task.setData(advInterval);
        return task;
    }

    public static OrderTask setPassword(String password) {
        SetPasswordTask task = new SetPasswordTask();
        task.setData(password);
        return task;
    }

    public static OrderTask setConnection(String connection) {
        SetConnectionTask task = new SetConnectionTask();
        task.setData(connection);
        return task;
    }

    public static OrderTask setClose() {
        ParamsWriteTask task = new ParamsWriteTask();
        task.close();
        return task;
    }

    public static OrderTask setAdvName(String advName) {
        SetAdvNameTask task = new SetAdvNameTask();
        task.setData(advName);
        return task;
    }

    public static OrderTask setDeviceUUID(String uuid) {
        SetDeviceUUIDTask task = new SetDeviceUUIDTask();
        task.setData(uuid);
        return task;
    }

    public static OrderTask setMajor(int major) {
        SetMajorTask task = new SetMajorTask();
        task.setData(major);
        return task;
    }

    public static OrderTask setMinor(int minor) {
        SetMinorTask task = new SetMinorTask();
        task.setData(minor);
        return task;
    }

    public static OrderTask setMeasurePower(int measurePower) {
        SetMeasurePowerTask task = new SetMeasurePowerTask();
        task.setData(measurePower);
        return task;
    }

    public static OrderTask setOvertime() {
        SetOvertimeTask task = new SetOvertimeTask();
        task.setData();
        return task;
    }

    public static OrderTask setTransmission(int transmission) {
        SetTransmissionTask task = new SetTransmissionTask();
        task.setData(transmission);
        return task;
    }

    public static OrderTask setSoftReboot() {
        SetSoftRebootTask task = new SetSoftRebootTask();
        return task;
    }

    public static OrderTask setThreeAxes(boolean enable) {
        ParamsWriteTask task = new ParamsWriteTask();
        task.setAxisEnable(enable);
        return task;
    }

    public static OrderTask setSerialID(String serialID) {
        SetSerialIDTask task = new SetSerialIDTask();
        task.setData(serialID);
        return task;
    }
}

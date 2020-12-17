package com.moko.support;

import com.moko.support.task.GetAdvInterval;
import com.moko.support.task.GetAdvName;
import com.moko.support.task.GetBattery;
import com.moko.support.task.GetChipModel;
import com.moko.support.task.GetConnection;
import com.moko.support.task.GetDeviceMac;
import com.moko.support.task.GetDeviceModel;
import com.moko.support.task.GetDeviceUUID;
import com.moko.support.task.GetFirmwareVersion;
import com.moko.support.task.GetHardwareVersion;
import com.moko.support.task.GetMajor;
import com.moko.support.task.GetManufacturer;
import com.moko.support.task.GetMeasurePower;
import com.moko.support.task.GetMinor;
import com.moko.support.task.GetProductDate;
import com.moko.support.task.GetRunntime;
import com.moko.support.task.GetSerialID;
import com.moko.support.task.GetSoftVersion;
import com.moko.support.task.GetTransmission;
import com.moko.support.task.OrderTask;
import com.moko.support.task.SetAdvInterval;
import com.moko.support.task.SetAdvName;
import com.moko.support.task.SetClose;
import com.moko.support.task.SetConnection;
import com.moko.support.task.SetDeviceUUID;
import com.moko.support.task.SetMajor;
import com.moko.support.task.SetMeasurePower;
import com.moko.support.task.SetMinor;
import com.moko.support.task.SetOvertime;
import com.moko.support.task.SetPassword;
import com.moko.support.task.SetSerialID;
import com.moko.support.task.SetSoftReboot;
import com.moko.support.task.SetThreeAxes;
import com.moko.support.task.SetTransmission;

public class OrderTaskAssembler {
    ///////////////////////////////////////////////////////////////////////////
    // READ
    ///////////////////////////////////////////////////////////////////////////

    public static OrderTask getBattery() {
        GetBattery task = new GetBattery();
        return task;
    }

    public static OrderTask getAdvInterval() {
        GetAdvInterval task = new GetAdvInterval();
        return task;
    }

    public static OrderTask getChipModel() {
        GetChipModel task = new GetChipModel();
        return task;
    }

    public static OrderTask getConnection() {
        GetConnection task = new GetConnection();
        return task;
    }

    public static OrderTask getDeviceModel() {
        GetDeviceModel task = new GetDeviceModel();
        return task;
    }

    public static OrderTask getManufacturer() {
        GetManufacturer task = new GetManufacturer();
        return task;
    }

    public static OrderTask getFirmwareVersion() {
        GetFirmwareVersion task = new GetFirmwareVersion();
        return task;
    }

    public static OrderTask getHardwareVersion() {
        GetHardwareVersion task = new GetHardwareVersion();
        return task;
    }

    public static OrderTask getProductDate() {
        GetProductDate task = new GetProductDate();
        return task;
    }

    public static OrderTask getDeviceMac() {
        GetDeviceMac task = new GetDeviceMac();
        return task;
    }

    public static OrderTask getAdvName() {
        GetAdvName task = new GetAdvName();
        return task;
    }

    public static OrderTask getDeviceUUID() {
        GetDeviceUUID task = new GetDeviceUUID();
        return task;
    }

    public static OrderTask getMajor() {
        GetMajor task = new GetMajor();
        return task;
    }

    public static OrderTask getMinor() {
        GetMinor task = new GetMinor();
        return task;
    }

    public static OrderTask getMeasurePower() {
        GetMeasurePower task = new GetMeasurePower();
        return task;
    }

    public static OrderTask getRunntime() {
        GetRunntime task = new GetRunntime();
        return task;
    }

    public static OrderTask getTransmission() {
        GetTransmission task = new GetTransmission();
        return task;
    }

    public static OrderTask getSoftVersion() {
        GetSoftVersion task = new GetSoftVersion();
        return task;
    }

    public static OrderTask getSerialID() {
        GetSerialID task = new GetSerialID();
        return task;
    }


    ///////////////////////////////////////////////////////////////////////////
    // WRITE
    ///////////////////////////////////////////////////////////////////////////

    public static OrderTask setAdvInterval(int advInterval) {
        SetAdvInterval task = new SetAdvInterval();
        task.setData(advInterval);
        return task;
    }

    public static OrderTask setPassword(String password) {
        SetPassword task = new SetPassword();
        task.setData(password);
        return task;
    }

    public static OrderTask setConnection(String connection) {
        SetConnection task = new SetConnection();
        task.setData(connection);
        return task;
    }

    public static OrderTask setClose() {
        SetClose task = new SetClose();
        task.setData();
        return task;
    }

    public static OrderTask setAdvName(String advName) {
        SetAdvName task = new SetAdvName();
        task.setData(advName);
        return task;
    }

    public static OrderTask setDeviceUUID(String uuid) {
        SetDeviceUUID task = new SetDeviceUUID();
        task.setData(uuid);
        return task;
    }

    public static OrderTask setMajor(int major) {
        SetMajor task = new SetMajor();
        task.setData(major);
        return task;
    }

    public static OrderTask setMinor(int minor) {
        SetMinor task = new SetMinor();
        task.setData(minor);
        return task;
    }

    public static OrderTask setMeasurePower(int measurePower) {
        SetMeasurePower task = new SetMeasurePower();
        task.setData(measurePower);
        return task;
    }

    public static OrderTask setOvertime() {
        SetOvertime task = new SetOvertime();
        task.setData();
        return task;
    }

    public static OrderTask setTransmission(int transmission) {
        SetTransmission task = new SetTransmission();
        task.setData(transmission);
        return task;
    }

    public static OrderTask setSoftReboot() {
        SetSoftReboot task = new SetSoftReboot();
        return task;
    }

    public static OrderTask setThreeAxes(boolean isOpen) {
        SetThreeAxes task = new SetThreeAxes();
        task.setData(isOpen);
        return task;
    }

    public static OrderTask setSerialID(String serialID) {
        SetSerialID task = new SetSerialID();
        task.setData(serialID);
        return task;
    }
}

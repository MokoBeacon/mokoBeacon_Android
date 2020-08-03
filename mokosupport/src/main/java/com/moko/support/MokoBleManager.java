package com.moko.support;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.support.annotation.NonNull;

import com.moko.support.callback.MokoResponseCallback;
import com.moko.support.log.LogModule;
import com.moko.support.utils.MokoUtils;

import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.BleManagerCallbacks;
import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.data.Data;

public class MokoBleManager extends BleManager<BleManagerCallbacks> {

    private MokoResponseCallback mMokoResponseCallback;
    private static MokoBleManager managerInstance = null;
    private final static UUID SERVICE_UUID = UUID.fromString("0000FF00-0000-1000-8000-00805F9B34FB");
    private final static UUID PARAMS_CONFIG_UUID = UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB");
    private final static UUID PASSWORD_UUID = UUID.fromString("0000FF06-0000-1000-8000-00805F9B34FB");
    private BluetoothGattCharacteristic passwordCharacteristic;
    private BluetoothGattCharacteristic paramsConfigCharacteristic;

    public static synchronized MokoBleManager getMokoBleManager(final Context context) {
        if (managerInstance == null) {
            managerInstance = new MokoBleManager(context);
        }
        return managerInstance;
    }

    @Override
    public void log(int priority, @NonNull String message) {
        LogModule.v(message);
    }

    public MokoBleManager(@NonNull Context context) {
        super(context);
    }

    public void setBeaconResponseCallback(MokoResponseCallback mMokoResponseCallback) {
        this.mMokoResponseCallback = mMokoResponseCallback;
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return new MokoBleManagerGattCallback();
    }

    public class MokoBleManagerGattCallback extends BleManagerGattCallback {
        @Override
        protected void initialize() {
        }

        @Override
        protected boolean isRequiredServiceSupported(@NonNull BluetoothGatt gatt) {
            final BluetoothGattService service = gatt.getService(SERVICE_UUID);
            if (service != null) {
                passwordCharacteristic = service.getCharacteristic(PASSWORD_UUID);
                paramsConfigCharacteristic = service.getCharacteristic(PARAMS_CONFIG_UUID);
                enablePasswordNotify();
                enableParamConfigNotify();
                return true;
            }
            return false;
        }

        @Override
        protected void onDeviceDisconnected() {

        }

        @Override
        protected void onCharacteristicWrite(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic) {
            LogModule.e("device to app : " + MokoUtils.bytesToHexString(characteristic.getValue()));
            mMokoResponseCallback.onCharacteristicWrite(characteristic.getValue());
        }

        @Override
        protected void onCharacteristicRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic) {
            LogModule.e("device to app : " + MokoUtils.bytesToHexString(characteristic.getValue()));
            mMokoResponseCallback.onCharacteristicRead(characteristic.getValue());
        }

        @Override
        protected void onDescriptorWrite(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattDescriptor descriptor) {
            LogModule.e("onDescriptorWrite");
            String characteristicUUIDStr = descriptor.getCharacteristic().getUuid().toString().toLowerCase();
            if (passwordCharacteristic.getUuid().toString().toLowerCase().equals(characteristicUUIDStr)) {
                LogModule.e("password notify opened");
            }
            if (paramsConfigCharacteristic.getUuid().toString().toLowerCase().equals(characteristicUUIDStr)) {
                LogModule.e("paramsConfig notify opened");
                gatt.requestMtu(247);
                mMokoResponseCallback.onServicesDiscovered(gatt);
            }
        }
    }

    public void enablePasswordNotify() {
        setIndicationCallback(passwordCharacteristic).with(new DataReceivedCallback() {
            @Override
            public void onDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
                final byte[] value = data.getValue();
                LogModule.e("onDataReceived");
                LogModule.e("device to app : " + MokoUtils.bytesToHexString(value));
                mMokoResponseCallback.onCharacteristicChanged(passwordCharacteristic, value);
            }
        });
        enableNotifications(passwordCharacteristic).enqueue();
    }

    public void disablePasswordNotify() {
        disableNotifications(passwordCharacteristic).enqueue();
    }

    public void enableParamConfigNotify() {
        setIndicationCallback(paramsConfigCharacteristic).with(new DataReceivedCallback() {
            @Override
            public void onDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
                final byte[] value = data.getValue();
                LogModule.e("onDataReceived");
                LogModule.e("device to app : " + MokoUtils.bytesToHexString(value));
                mMokoResponseCallback.onCharacteristicChanged(paramsConfigCharacteristic, value);
            }
        });
        enableNotifications(paramsConfigCharacteristic).enqueue();
    }

    public void disableParamConfigNotify() {
        disableNotifications(paramsConfigCharacteristic).enqueue();
    }
}

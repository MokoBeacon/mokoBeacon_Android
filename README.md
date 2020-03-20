# MokoBeacon Android SDK Instruction DOC（English）

----

## 1. Import project

**1.1 Import "Module mokosupport" to root directory**

**1.2 Edit "settings.gradle" file**

```
include ':app', ':mokosupport'
```

**1.3 Edit "build.gradle" file under the APP project**

	```
	dependencies {
		...
		compile project(path: ':mokosupport')
	}
	```

----

## 2. How to use

Initialize sdk at project initialization

```
MokoSupport.getInstance().init(getApplicationContext());
```

SDK provides three main functions:

* Scan the device(MkiBeacon);
* Connect to the device(MkiBeacon);
* Send and receive data.

### 2.1 Scan the device (MkiBeacon)

* **Start scanning**

```
MokoSupport.getInstance().startScanDevice(callback);
```

* **End scanning**

```
MokoSupport.getInstance().stopScanDevice();
```

* **Implement the scanning callback interface**

```
/**
 * @ClassPath com.moko.support.callback.MokoScanDeviceCallback
 */
public interface MokoScanDeviceCallback {
    void onStartScan();

    void onScanDevice(DeviceInfo device);

    void onStopScan();
}
```
* **Analysis `DeviceInfo` ; inferred `BeaconInfo`**

```
BeaconInfo beaconInfo = new BeaconInfoParseableImpl().parseDeviceInfo(device);
```

Please refer to "Demo Project" to use `BeaconInfoParseableImpl` class. You can get some basic information from `BeaconInfo`, such as "Device Name", "MAC address", "RSSI" .



### 2.2 Connect to the device(MkiBeacon)

* **Connect to the device**

```
MokoSupport.getInstance().connDevice(context, address, mokoConnStateCallback);
```

When connecting to the device, context, MAC address and callback interface of connection status should be transferred in.

```
public interface MokoConnStateCallback {

    /**
     * @Description    Connecting succeeds
     */
    void onConnectSuccess();

    /**
     * @Description   Disconnect
     */
    void onDisConnected();
}
```

"Demo Project" implements callback interface in Service. It broadcasts status to Activity after receiving the status, and could send and receive data after connecting to the device.

### 2.3 Send and receive data.

All the request data is encapsulated into **TASK**, and sent to the device in a **QUEUE** way.
SDK gets task status from task callback (`MokoOrderTaskCallback`) after sending tasks successfully.

* **Task**

At present, all the tasks sent from the SDK can be divided into 4 types:

> 1.  READ：Readable
> 2.  WRITE：Writable
> 3.  NOTIFY：Can be listened( Need to enable the notification property of the relevant characteristic values)
> 4.  WRITE_NO_RESPONSE：After enabling the notification property, send data to the device and listen to the data returned by device.

Encapsulated tasks are as follows:

|Task Class|Task Type|Function
|----|----|----
|`BatteryTask`|READ|Get battery capacity
|`FirmnameTask`|READ|Get manufacturer
|`DevicenameTask`|READ|Get product model
|`IBeaconDateTask`|READ|Get production date
|`HardwareVersionTask`|READ|Get hardware version
|`FirmwareVersionTask` |READ|Get firmware version
|`SystemMarkTask`|READ|Get system ID
|`IEEEInfoTask`|READ|Get IEEE information
| `NotifyTask`|NOTIFY|Enable notification of running time, chip model and 3-axes accelerometer data
|`RunntimeTask`|WRITE_NO_RESPONSE|Listen to the running time
|`ChipModelTask`|WRITE_NO_RESPONSE|Listen to the chip model
|`ThreeAxesTask`|WRITE_NO_RESPONSE|Listen to the 3-axes accelerometer data
|`ChangePasswordTask`|NOTIFY|Enable notification of modifying password
|`ChangePasswordTask`|WRITE_NO_RESPONSE|Modify password and listen to the status of modifying password
|`IBeaconUuidTask`|READ|Get UUID
|`IBeaconUuidTask`|WRITE|Configure UUID
|`MajorTask` |READ|Get Major
|`MajorTask` |WRITE|Configure Major
|`MinorTask`|READ|Get Minor
|`MinorTask`|WRITE|Configure Minor
|`MeasurePowerTask`|READ|Get Measured Power(RSSI@1m)
|`MeasurePowerTask`|WRITE|Configure Measured Power(RSSI@1m)
|`TransmissionTask`|READ|Get Tx Power
|`TransmissionTask`|WRITE|Configure Tx Power
|`BroadcastingIntervalTask`|READ|Get Broadcasting Interval
|`BroadcastingIntervalTask`|WRITE|Configure Broadcasting Interval
|`SerialIDTask`|READ|Get Device ID
|`SerialIDTask`|WRITE|Configure Device ID
|`IBeaconNameTask`|READ|Get Device Name
|`IBeaconNameTask`|WRITE|Configure Device Name
|`ConnectionModeTask`|READ|Get connection status
|`ConnectionModeTask`|WRITE|Configure connection status
|`SoftRebootModeTask`|WRITE|Reset Device
|`IBeaconMacTask`|READ|Get MAC address
|`OvertimeTask`|WRITE|Device will disconnect automatically after 1 minute without any configuration if it has been connected successfully; or device disconnects automatically after 20 seconds if it has not been connected yet.


* **Create tasks**

The task callback (`MokoOrderTaskCallback`) and task type need to be passed when creating a task. Some tasks also need corresponding parameters to be passed.

Examples of creating tasks are as follows:

```
    // Get Battery Capacity
    public OrderTask getBattery() {
        BatteryTask batteryTask = new BatteryTask(this, OrderTask.RESPONSE_TYPE_READ);
        return batteryTask;
    }
    ...
    // Modify Password
    public OrderTask setChangePassword(String password) {
        ChangePasswordTask changePasswordTask = new ChangePasswordTask(this, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        changePasswordTask.setData(password);
        return changePasswordTask;
    }
```

* **Send tasks**

```
MokoSupport.getInstance().sendOrder(OrderTask... orderTasks);
```

The task can be one or more.

* **Task callback**

```
/**
 * @ClassPath com.moko.support.callback.OrderCallback
 */
public interface MokoOrderTaskCallback {

    void onOrderResult(OrderType orderType, byte[] value);

    void onOrderTimeout(OrderType orderType);

    void onOrderFinish();
}
```
`void onOrderResult(OrderType orderType, byte[] value);`


	 After the task is sent to the device, the data returned by the device can be obtained by using the `onOrderResult` function, and you can determine witch class the task is according to the `OrderType` function. The `value` is the returned data.

`void onOrderTimeout(OrderType orderType);`

	Every task has a default timeout of 3 seconds to prevent the device from failing to return data due to a fault and the fail will cause other tasks in the queue can not execute normally. After the timeout, the `onOrderTimeout` will be called back. You can determine witch class the task is according to the `OrderType` function and then the next task continues.

`void onOrderFinish();`

	When the task in the queue is empty, `onOrderFinish` will be called back.

* **Listening task**

If the task belongs to `NOTIFY` and ` WRITE_NO_RESPONSE` task has been sent, the task is in listening state. When there is data returned from the device, the data will be sent in the form of broadcast, and the action of receiving broadcast is `MokoConstants.ACTION_RESPONSE_NOTIFY`.

```
String action = intent.getAction();
...
if (MokoConstants.ACTION_RESPONSE_NOTIFY.equals(action)) {
                    OrderType orderType = (OrderType) intent.getSerializableExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TYPE);
                    byte[] value = intent.getByteArrayExtra(MokoConstants.EXTRA_KEY_RESPONSE_VALUE);
                    ...
                }
```
Get `OrderType` and` value` from `onReceive` intent, and the corresponding key values were `MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TYPE` and `MokoConstants.EXTRA_KEY_RESPONSE_VALUE`。

## 3. Special instructions

> 1. AndroidManifest.xml of SDK has declared to access SD card and get Bluetooth permissions.
> 2. The SDK comes with logging, and if you want to view the log in the SD card, please to use "LogModule". The log path is : root directory of SD card/mokoSupport/mokoLog. It only records the log of the day and the day before.
> 3. Just connecting to the device successfully, it needs to delay 1 second before sending data, otherwise the device can not return data normally.
> 4. We suggest that sending and receiving data should be executed in the "Service". There will be a certain delay when the device returns data, and you can broadcast data to the "Activity" after receiving in the "Service". Please refer to the "Demo Project".

















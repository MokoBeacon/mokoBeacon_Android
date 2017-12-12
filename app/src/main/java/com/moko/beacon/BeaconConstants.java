package com.moko.beacon;

public class BeaconConstants {
    // data time pattern
    public static final String PATTERN_HH_MM = "HH:mm";
    public static final String PATTERN_YYYY_MM_DD = "yyyy-MM-dd";
    public static final String PATTERN_MM_DD = "MM/dd";
    public static final String PATTERN_MM_DD_2 = "MM-dd";
    public static final String PATTERN_YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
    // action

    // 搜索到的设备信息数据
    public static final String ACTION_BLE_DEVICES_DATA = "fitpolo_action_ble_devices_data";

    // sp
    public static final String SP_NAME = "sp_name_beacon";
    public static final String SP_KEY_DEVICE_ADDRESS = "sp_key_device_address";

    // extra_key
    // 设备列表
    public static final String EXTRA_KEY_ALARM = "extra_key_alarm";
    public static final String EXTRA_CONN_COUNT = "extra_conn_count";
    // request_code
    public static final int REQUEST_CODE_ENABLE_BT = 1001;
}

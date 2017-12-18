package com.moko.beacon;

public class BeaconConstants {
    // data time pattern
    public static final String PATTERN_HH_MM = "HH:mm";
    public static final String PATTERN_YYYY_MM_DD = "yyyy-MM-dd";
    public static final String PATTERN_MM_DD = "MM/dd";
    public static final String PATTERN_MM_DD_2 = "MM-dd";
    public static final String PATTERN_YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
    // action
    public static final String ACTION_CONNECT_SUCCESS = "ACTION_CONNECT_SUCCESS";
    public static final String ACTION_CONNECT_DISCONNECTED = "ACTION_CONNECT_DISCONNECTED";
    public static final String ACTION_RESPONSE_SUCCESS = "ACTION_RESPONSE_SUCCESS";
    public static final String ACTION_RESPONSE_TIMEOUT = "ACTION_RESPONSE_TIMEOUT";
    public static final String ACTION_RESPONSE_FINISH = "ACTION_RESPONSE_FINISH";
    // sp
    public static final String SP_NAME = "sp_name_beacon";

    public static final String SP_KEY_DEVICE_ADDRESS = "sp_key_device_address";
    // extra_key
    // 设备列表
    public static final String EXTRA_KEY_RESPONSE_ORDER_TYPE = "EXTRA_KEY_RESPONSE_ORDER_TYPE";
    public static final String EXTRA_KEY_RESPONSE_VALUE = "EXTRA_KEY_RESPONSE_VALUE";
    public static final String EXTRA_KEY_DEVICE_PARAM = "EXTRA_KEY_DEVICE_PARAM";
    public static final String EXTRA_KEY_DEVICE_INFO = "EXTRA_KEY_DEVICE_INFO";
    public static final String EXTRA_KEY_DEVICE_UUID = "EXTRA_KEY_DEVICE_UUID";
    // request_code
    public static final int REQUEST_CODE_ENABLE_BT = 1001;
    public static final int REQUEST_CODE_DEVICE_INFO = 101;
    public static final int REQUEST_CODE_SET_UUID = 102;
    // result_code
    public static final int RESULT_CONN_DISCONNECTED = 2;
}

package com.moko.beacon;

public class BeaconConstants {
    // data time pattern
    public static final String PATTERN_HH_MM = "HH:mm";
    public static final String PATTERN_HH_MM_SS = "HH:mm:ss";
    public static final String PATTERN_YYYY_MM_DD = "yyyy-MM-dd";
    public static final String PATTERN_MM_DD = "MM/dd";
    public static final String PATTERN_MM_DD_2 = "MM-dd";
    public static final String PATTERN_YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
    // action
    // sp
    public static final String SP_NAME = "sp_name_beacon";

    public static final String SP_KEY_DEVICE_ADDRESS = "sp_key_device_address";
    // extra_key
    // 设备列表\
    public static final String EXTRA_KEY_DEVICE_PARAM = "EXTRA_KEY_DEVICE_PARAM";
    public static final String EXTRA_KEY_DEVICE_INFO = "EXTRA_KEY_DEVICE_INFO";
    public static final String EXTRA_KEY_DEVICE_UUID = "EXTRA_KEY_DEVICE_UUID";
    public static final String EXTRA_KEY_DEVICE_MAJOR = "EXTRA_KEY_DEVICE_MAJOR";
    public static final String EXTRA_KEY_DEVICE_MINOR = "EXTRA_KEY_DEVICE_MINOR";
    public static final String EXTRA_KEY_DEVICE_MEASURE_POWER = "EXTRA_KEY_DEVICE_MEASURE_POWER";
    public static final String EXTRA_KEY_DEVICE_TRANSMISSION = "EXTRA_KEY_DEVICE_TRANSMISSION";
    public static final String EXTRA_KEY_DEVICE_BROADCASTINTERVAL = "EXTRA_KEY_DEVICE_BROADCASTINTERVAL";
    public static final String EXTRA_KEY_DEVICE_DEVICE_ID = "EXTRA_KEY_DEVICE_DEVICE_ID";
    public static final String EXTRA_KEY_DEVICE_IBEACON_NAME = "EXTRA_KEY_DEVICE_IBEACON_NAME";
    public static final String EXTRA_KEY_DEVICE_CONNECTION_MODE = "EXTRA_KEY_DEVICE_CONNECTION_MODE";
    public static final String EXTRA_KEY_DEVICE_PASSWORD = "EXTRA_KEY_DEVICE_PASSWORD";
    // request_code
    public static final int REQUEST_CODE_DEVICE_INFO = 101;
    public static final int REQUEST_CODE_SET_UUID = 102;
    public static final int REQUEST_CODE_SET_MAJOR = 103;
    public static final int REQUEST_CODE_SET_MINOR = 104;
    public static final int REQUEST_CODE_SET_MEASURE_POWER = 105;
    public static final int REQUEST_CODE_SET_TRANSMISSION = 106;
    public static final int REQUEST_CODE_SET_BROADCASTINTERVAL = 107;
    public static final int REQUEST_CODE_SET_DEVICE_ID = 108;
    public static final int REQUEST_CODE_SET_IBEACON_NAME = 109;
    public static final int REQUEST_CODE_SET_CONNECTION_MODE = 110;
    public static final int REQUEST_CODE_SET_PASSWORD = 111;
    public static final int REQUEST_CODE_SET_SYSTEM_INFO = 112;
    public static final int REQUEST_CODE_SET_THREE_AXIS = 113;
    // result_code
    public static final int RESULT_CONN_DISCONNECTED = 2;
}

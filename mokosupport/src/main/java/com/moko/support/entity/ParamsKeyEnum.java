package com.moko.support.entity;


import java.io.Serializable;

public enum ParamsKeyEnum implements Serializable {
    GET_CHIP_MODEL(0x5B),
    GET_RUNTIME(0x59),
    SET_CLOSE(0x6D),
    GET_THREE_AXES(0x6C),
    ;

    private int paramsKey;

    ParamsKeyEnum(int paramsKey) {
        this.paramsKey = paramsKey;
    }


    public int getParamsKey() {
        return paramsKey;
    }

    public static ParamsKeyEnum fromParamKey(int configKey) {
        for (ParamsKeyEnum paramsKeyEnum : ParamsKeyEnum.values()) {
            if (paramsKeyEnum.getParamsKey() == configKey) {
                return paramsKeyEnum;
            }
        }
        return null;
    }
}

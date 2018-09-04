package com.moko.beacon.entity;

/**
 * Created by liuwenzheng on 2018/7/13.
 */

public enum IBeaconType {
    IBEACON("0000ff00"),
    IBEACON_THREE_AXIS("0000ff01"),

    ;

    IBeaconType(String service) {
        this.service = service;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    private String service;

}

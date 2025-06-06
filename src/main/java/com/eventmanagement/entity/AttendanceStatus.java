package com.eventmanagement.entity;

//AttendanceStatus enum for define Attendance status
public enum AttendanceStatus {
    GOING("GOING"),
    MAYBE("MAYBE"),
    DECLINED("DECLINED");

    private final String value;

    AttendanceStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
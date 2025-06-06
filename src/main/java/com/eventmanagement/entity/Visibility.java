package com.eventmanagement.entity;


//Visibility enum for define Visibility status
public enum Visibility {
    PUBLIC("PUBLIC"),
    PRIVATE("PRIVATE");

    private final String value;

    Visibility(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

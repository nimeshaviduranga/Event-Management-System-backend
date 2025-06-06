package com.eventmanagement.entity;

import org.springframework.security.core.GrantedAuthority;

//Role enum class for define User roles
public enum Role implements GrantedAuthority {
    USER("USER"),
    ADMIN("ADMIN");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    @Override
    public String getAuthority() {
        return "ROLE_" + value;
    }

    public String getValue() {
        return value;
    }
}
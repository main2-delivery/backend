package com.main2.delivery.common.type;

public enum Role {
    CUSTOMER,
    OWNER,
    ADMIN;

    public String authority() {
        return  "ROLE_" + name();
    }
}
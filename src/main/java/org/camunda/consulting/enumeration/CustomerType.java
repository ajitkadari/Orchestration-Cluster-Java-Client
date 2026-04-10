package org.camunda.consulting.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;

public enum CustomerType {
    VIP,
    REGULAR;

    @JsonCreator
    public static CustomerType fromValue(String value) {
        if (value == null) {
            return null;
        }

        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported customer type: " + value));
    }
}


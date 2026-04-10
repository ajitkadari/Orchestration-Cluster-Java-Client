package org.camunda.consulting.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;

public enum ItemCategory {
    ELECTRONICS;

    @JsonCreator
    public static ItemCategory fromValue(String value) {
        if (value == null) {
            return null;
        }

        return Arrays.stream(values())
                .filter(category -> category.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported item category: " + value));
    }
}

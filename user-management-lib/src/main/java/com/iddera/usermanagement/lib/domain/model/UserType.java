package com.iddera.usermanagement.lib.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.stream.Stream;

import static com.iddera.commons.utils.FunctionUtil.isNullOrEmpty;


public enum UserType {
    DOCTOR,
    CLIENT,
    ADMIN,
    SYSTEM,
    UNKNOWN;

    @JsonCreator
    public static UserType create(String value) {
        if (isNullOrEmpty(value)) {
            return null;
        }

        return Stream.of(UserType.values())
                .filter(e -> e.toString().equalsIgnoreCase(value)).findFirst()
                .orElse(UNKNOWN);
    }
}

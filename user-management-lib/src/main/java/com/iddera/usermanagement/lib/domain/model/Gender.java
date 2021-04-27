package com.iddera.usermanagement.lib.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.stream.Stream;

import static com.iddera.commons.utils.FunctionUtil.isNullOrEmpty;

public enum Gender {
    MALE, FEMALE, OTHERS, UNKNOWN;

    @JsonCreator
    public static Gender create(String value) {
        if (isNullOrEmpty(value)) {
            return null;
        }

        return Stream.of(Gender.values())
                .filter(e -> e.toString().equalsIgnoreCase(value)).findFirst()
                .orElse(UNKNOWN);
    }
}

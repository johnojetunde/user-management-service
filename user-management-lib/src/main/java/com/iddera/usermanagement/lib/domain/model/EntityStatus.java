package com.iddera.usermanagement.lib.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.stream.Stream;

import static com.iddera.usermanagement.lib.domain.utils.FunctionUtil.isNullOrEmpty;

public enum EntityStatus {
    ACTIVE, INACTIVE, UNKNOWN;

    @JsonCreator
    public static EntityStatus create(String value) {
        if (isNullOrEmpty(value)) {
            return null;
        }

        return Stream.of(EntityStatus.values())
                .filter(e -> e.toString().equalsIgnoreCase(value)).findFirst()
                .orElse(UNKNOWN);
    }
}

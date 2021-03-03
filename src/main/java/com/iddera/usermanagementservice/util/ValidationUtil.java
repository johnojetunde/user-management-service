package com.iddera.usermanagementservice.util;

import lombok.experimental.UtilityClass;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.iddera.usermanagementservice.util.ExceptionUtil.handleCreateBadRequest;

@UtilityClass
public class ValidationUtil {
    public void ensureIsUnique(Long userId,
                               String input,
                               BiFunction<Long, String, Boolean> uniqueChecker,
                               String errorMessage) {
        if (uniqueChecker.apply(userId, input)) {
            throw handleCreateBadRequest(errorMessage);
        }
    }

    public void ensureIsUnique(String input,
                               Function<String, Boolean> uniqueChecker,
                               String errorMessage) {
        if (uniqueChecker.apply(input)) {
            throw handleCreateBadRequest(errorMessage);
        }
    }

    public void ensureIsNotUnique(String secondInput,
                                  String input,
                                  BiFunction<String, String, Boolean> uniqueChecker,
                                  String errorMessage) {
        if (!uniqueChecker.apply(secondInput, input)) {
            throw handleCreateBadRequest(errorMessage);
        }
    }
}

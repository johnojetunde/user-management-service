package com.iddera.usermanagement.lib.domain.utils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class FunctionUtil {
    private FunctionUtil() {
    }

    public static <T> Stream<T> emptyIfNullStream(Collection<T> list) {
        return (list == null) ? Stream.empty() : list.stream();
    }

    public static <T> Collection<T> emptyIfNull(Collection<T> list) {
        return (list == null) ? List.of() : list;
    }

    public static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }
}

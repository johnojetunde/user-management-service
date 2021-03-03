package com.iddera.usermanagementservice.util;

import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.stream.Stream;

@UtilityClass
public class FunctionUtil {
    public static <T> Stream<T> emptyIfNullStream(Collection<T> list) {
        return (list == null) ? Stream.empty() : list.stream();
    }
}

package com.iddera.usermanagement.api.domain.exception;

import com.iddera.commons.exception.ApiException;

import java.util.Map;

public class UserManagementException extends ApiException {
    public UserManagementException() {
    }

    public UserManagementException(String message) {
        super(message);
    }

    public UserManagementException(int code, String message, Throwable throwable) {
        super(message, throwable, code, Map.of());
    }
}

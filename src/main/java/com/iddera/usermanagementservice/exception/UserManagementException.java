package com.iddera.usermanagementservice.exception;

import java.util.List;
import java.util.Map;

import static java.lang.String.format;

public class UserManagementException extends ApiException {
    public UserManagementException() {
    }

    public UserManagementException(Throwable throwable) {
        super(throwable);
    }

    public UserManagementException(String message) {
        super(message);
    }

    public UserManagementException(String message, Object... args) {
        super(format(message, args));
    }

    public UserManagementException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public UserManagementException(String message, Throwable throwable, int code, Map<String, List<String>> responseHeaders, String responseBody) {
        super(message, throwable, code, responseHeaders, responseBody);
    }

    public UserManagementException(String message, int code, Map<String, List<String>> responseHeaders, String responseBody) {
        super(message, code, responseHeaders, responseBody);
    }

    public UserManagementException(String message, Throwable throwable, int code, Map<String, List<String>> responseHeaders) {
        super(message, throwable, code, responseHeaders);
    }

    public UserManagementException(int code, Map<String, List<String>> responseHeaders, String responseBody) {
        super(code, responseHeaders, responseBody);
    }

    public UserManagementException(int code, String message) {
        super(code, message);
    }

    public UserManagementException(int code, String message, Map<String, List<String>> responseHeaders, String responseBody) {
        super(code, message, responseHeaders, responseBody);
    }
}

package com.iddera.usermanagement.api.domain.exception;

import com.iddera.commons.utils.ExceptionUtil;
import org.springframework.stereotype.Service;

@Service
public class UserManagementExceptionService extends ExceptionUtil<UserManagementException> {

    @Override
    public UserManagementException createCustomException(String s, Throwable throwable, int i) {
        return new UserManagementException(i, s, throwable);
    }

    @Override
    public Class<UserManagementException> customExceptionClass() {
        return UserManagementException.class;
    }
}

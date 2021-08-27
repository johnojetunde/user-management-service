package com.iddera.usermanagement.api.domain.service.abstracts;

import com.iddera.usermanagement.api.domain.exception.UserManagementExceptionService;
import com.iddera.usermanagement.api.persistence.entity.User;
import com.iddera.usermanagement.lib.app.request.PinUpdate;

import java.util.function.BiPredicate;

public interface PinUpdateService {
    boolean useStrategy(User user);

    default User updateOrCreatePin(User user, PinUpdate pinUpdate) {
        validateField(user, pinUpdate);
        return updatePin(user, pinUpdate);
    }

    UserManagementExceptionService exception();

    void validateField(User user, PinUpdate pinUpdate);

    User updatePin(User user, PinUpdate pinUpdate);

    default void ensureRequiredFieldIsSet(BiPredicate<User, PinUpdate> biPredicate,
                                          User user,
                                          PinUpdate pinUpdate,
                                          String errorMessage) {
        if (biPredicate.test(user, pinUpdate))
            throw exception().handleCreateBadRequest(errorMessage);
    }
}

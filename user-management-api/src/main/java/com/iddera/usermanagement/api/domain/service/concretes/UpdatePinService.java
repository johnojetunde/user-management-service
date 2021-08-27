package com.iddera.usermanagement.api.domain.service.concretes;

import com.iddera.usermanagement.api.domain.exception.UserManagementExceptionService;
import com.iddera.usermanagement.api.domain.service.abstracts.PinUpdateService;
import com.iddera.usermanagement.api.persistence.entity.User;
import com.iddera.usermanagement.lib.app.request.PinUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.function.BiPredicate;

@Service
@RequiredArgsConstructor
public class UpdatePinService implements PinUpdateService {

    private final PasswordEncoder passwordEncoder;
    private final UserManagementExceptionService exceptionService;

    @Override
    public boolean useStrategy(User user) {
        return user.isPinSet();
    }

    @Override
    public void validateField(User user, PinUpdate pinUpdate) {
        BiPredicate<User, PinUpdate> predicate = (User u, PinUpdate p) ->
                u.isPinSet() && p.getCurrentPin() == null;
        ensureRequiredFieldIsSet(predicate, user, pinUpdate, "Current pin is required");
    }

    @Override
    public User updatePin(User user, PinUpdate pinUpdate) {
        if (!passwordEncoder.matches(pinUpdate.getCurrentPin(), user.getPin())) {
            throw exceptionService.handleCreateUnAuthorized("Invalid current pin");
        }
        String hashPin = passwordEncoder.encode(pinUpdate.getNewPin());
        user.setPin(hashPin);

        return user;
    }

    @Override
    public UserManagementExceptionService exception() {
        return exceptionService;
    }
}

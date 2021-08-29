package com.iddera.usermanagement.api.domain.service.concretes;

import com.iddera.usermanagement.api.domain.exception.UserManagementExceptionService;
import com.iddera.usermanagement.api.domain.service.abstracts.PinService;
import com.iddera.usermanagement.api.domain.service.abstracts.PinUpdateService;
import com.iddera.usermanagement.api.persistence.entity.User;
import com.iddera.usermanagement.api.persistence.repository.UserRepository;
import com.iddera.usermanagement.lib.app.request.PinUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.iddera.commons.utils.FunctionUtil.emptyIfNullStream;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@RequiredArgsConstructor
@Service
public class UserPinService implements PinService {

    private final UserRepository userRepository;
    private final UserManagementExceptionService exceptionService;
    private final List<PinUpdateService> updateServiceList;
    private final UserEmailService userEmailService;

    @Override
    public CompletableFuture<String> createOrUpdatePin(PinUpdate pinRequest,
                                                       String username) {
        return supplyAsync(() -> {
            var user = userRepository.findByUsername(username)
                    .orElseThrow(() -> exceptionService.handleCreateBadRequest("Invalid user %s", username));

            var pinUpdateService = getUpdateService(user);
            user = pinUpdateService.updateOrCreatePin(user, pinRequest);

            userRepository.save(user);
            userEmailService.sendPinNotification(user);
            return "PIN Successfully updated";
        });
    }

    PinUpdateService getUpdateService(User user) {
        return emptyIfNullStream(updateServiceList)
                .filter(p -> p.useStrategy(user))
                .findFirst()
                .orElseThrow(() -> exceptionService.handleCreateException("Error retrieving implementation for PinUpdate"));
    }
}

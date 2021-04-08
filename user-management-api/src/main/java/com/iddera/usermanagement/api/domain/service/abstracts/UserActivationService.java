package com.iddera.usermanagement.api.domain.service.abstracts;

import com.iddera.usermanagement.lib.app.request.UserVerificationRequest;
import com.iddera.usermanagement.lib.domain.model.UserModel;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface UserActivationService {
    CompletableFuture<UserModel> verifyUser(UserVerificationRequest userVerificationRequest);

    Map<String, Object> getActivateUserProperties(String username, String token);

}

package com.iddera.usermanagement.api.domain.service.abstracts;

import com.iddera.usermanagement.lib.app.request.ChangeUserPasswordRequest;
import com.iddera.usermanagement.lib.app.request.ForgotPasswordRequest;
import com.iddera.usermanagement.lib.domain.model.UserModel;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public interface UserPasswordService {
    CompletableFuture<UserModel> forgotPassword(String username, Locale locale);

    CompletableFuture<UserModel> resetPassword(Long id, ForgotPasswordRequest forgotPasswordRequest, Locale locale);

    CompletableFuture<UserModel> changePassword(Long userId, ChangeUserPasswordRequest changeUserPasswordRequest);
}

package com.iddera.usermanagement.api.domain.service.abstracts;

import com.iddera.usermanagement.lib.app.request.ChangeUserPasswordRequest;
import com.iddera.usermanagement.lib.app.request.EmailModel;
import com.iddera.usermanagement.lib.app.request.ForgotPasswordRequest;
import com.iddera.usermanagement.lib.domain.model.UserModel;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public interface UserPasswordService {
    CompletableFuture<UserModel> initiatePasswordReset(EmailModel emailModel, Locale locale);

    CompletableFuture<UserModel> resetPassword(ForgotPasswordRequest forgotPasswordRequest, Locale locale);

    CompletableFuture<UserModel> changePassword(Long userId, ChangeUserPasswordRequest changeUserPasswordRequest);
}

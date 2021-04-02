package com.iddera.usermanagement.api.domain.service.abstracts;

import com.iddera.usermanagement.lib.app.request.*;
import com.iddera.usermanagement.lib.domain.model.UserModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public interface UserService {
    CompletableFuture<UserModel> create(UserRequest request, Locale locale);

    CompletableFuture<UserModel> update(Long userId, UserUpdateRequest request);

    CompletableFuture<Page<UserModel>> getAll(Pageable pageable);

    CompletableFuture<UserModel> getById(Long userId);

    CompletableFuture<UserModel> getByUserName(String username);

    CompletableFuture<UserModel> forgotPassword(String username,Locale locale);

    CompletableFuture<UserModel> resetPassword(Long id, ForgotPasswordRequest forgotPasswordRequest, Locale locale);

    CompletableFuture<UserModel> changePassword(Long userId, ChangeUserPasswordRequest changeUserPasswordRequest);

    CompletableFuture<UserModel> verifyUser(UserVerificationRequest userVerificationRequest);
}
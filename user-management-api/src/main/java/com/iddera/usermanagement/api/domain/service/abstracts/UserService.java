package com.iddera.usermanagement.api.domain.service.abstracts;

import com.iddera.usermanagement.lib.app.request.ChangeUserPasswordRequest;
import com.iddera.usermanagement.lib.app.request.UserRequest;
import com.iddera.usermanagement.lib.app.request.UserUpdateRequest;
import com.iddera.usermanagement.lib.app.request.UserVerificationRequest;
import com.iddera.usermanagement.lib.domain.model.UserModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.concurrent.CompletableFuture;

public interface UserService {
    CompletableFuture<UserModel> create(UserRequest request);

    CompletableFuture<UserModel> update(Long userId, UserUpdateRequest request);

    CompletableFuture<Page<UserModel>> getAll(Pageable pageable);

    CompletableFuture<UserModel> getById(Long userId);

    CompletableFuture<UserModel> getByUserName(String username);

    CompletableFuture<UserModel> changePassword(Long userId, ChangeUserPasswordRequest changeUserPasswordRequest);

    CompletableFuture<UserModel> verifyUser(UserVerificationRequest userVerificationRequest);
}
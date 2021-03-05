package com.iddera.usermanagementservice.service.abstracts;

import com.iddera.usermanagementservice.model.UserModel;
import com.iddera.usermanagementservice.request.ChangeUserPasswordRequest;
import com.iddera.usermanagementservice.request.UserRequest;
import com.iddera.usermanagementservice.request.UserUpdateRequest;
import com.iddera.usermanagementservice.request.UserVerificationRequest;
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
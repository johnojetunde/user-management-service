package com.iddera.usermanagement.api.domain.service.abstracts;

import com.iddera.usermanagement.lib.app.request.UserRequest;
import com.iddera.usermanagement.lib.app.request.UserUpdateRequest;
import com.iddera.usermanagement.lib.app.request.UserVerificationRequest;
import com.iddera.usermanagement.lib.domain.model.UserModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.security.Principal;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public interface UserService {
    CompletableFuture<UserModel> create(UserRequest request, Locale locale);

    CompletableFuture<UserModel> update(Long userId, UserUpdateRequest request);

    CompletableFuture<Page<UserModel>> getAll(Pageable pageable);

    CompletableFuture<UserModel> getById(Long userId);

    CompletableFuture<UserModel> getByUserName(String username);

    CompletableFuture<UserModel> getUserDetails(Principal principal);

    CompletableFuture<UserModel> verifyUser(UserVerificationRequest userVerificationRequest);
}
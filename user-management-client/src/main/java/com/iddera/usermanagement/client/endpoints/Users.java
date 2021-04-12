package com.iddera.usermanagement.client.endpoints;

import com.iddera.client.model.Page;
import com.iddera.client.model.ResponseModel;
import com.iddera.client.util.ErrorHandler;
import com.iddera.usermanagement.client.retrofits.UserClient;
import com.iddera.usermanagement.lib.app.request.ChangeUserPasswordRequest;
import com.iddera.usermanagement.lib.app.request.UserRequest;
import com.iddera.usermanagement.lib.app.request.UserVerificationRequest;
import com.iddera.usermanagement.lib.domain.model.UserModel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class Users {
    private final UserClient userClient;

    public CompletableFuture<ResponseModel<UserModel>> create(@NonNull UserRequest request) {
        return userClient.create(request)
                .handleAsync(ErrorHandler::handleException);
    }

    public CompletableFuture<ResponseModel<UserModel>> getById(@NonNull Long id) {
        return userClient.getById(id)
                .handleAsync(ErrorHandler::handleException);
    }

    public CompletableFuture<ResponseModel<UserModel>> getByUsername(@NonNull String username) {
        return userClient.getByUsername(username)
                .handleAsync(ErrorHandler::handleException);
    }

    public CompletableFuture<ResponseModel<UserModel>> changePassword(@NonNull Long id, @NonNull ChangeUserPasswordRequest request) {
        return userClient.changePassword(id, request)
                .handleAsync(ErrorHandler::handleException);
    }

    public CompletableFuture<ResponseModel<UserModel>> verifyEmail(@NonNull UserVerificationRequest request) {
        return userClient.verifyEmail(request)
                .handleAsync(ErrorHandler::handleException);
    }

    public CompletableFuture<ResponseModel<Page<UserModel>>> getAll(Long pageNumber,
                                                                    Long pageSize) {
        return userClient.getAll(pageNumber, pageSize)
                .handleAsync(ErrorHandler::handleException);
    }

    public CompletableFuture<ResponseModel<UserModel>> getUserDetails(@NonNull String bearerToken){
        return userClient.getUserDetails(bearerToken)
                .handleAsync(ErrorHandler::handleException);
    }
}

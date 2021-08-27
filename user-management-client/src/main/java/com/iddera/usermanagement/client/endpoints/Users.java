package com.iddera.usermanagement.client.endpoints;

import com.iddera.client.model.Page;
import com.iddera.client.model.ResponseModel;
import com.iddera.client.util.ErrorHandler;
import com.iddera.usermanagement.client.retrofits.UserClient;
import com.iddera.usermanagement.lib.app.request.*;
import com.iddera.usermanagement.lib.app.response.EmailValidationResponse;
import com.iddera.usermanagement.lib.domain.model.LoginModel;
import com.iddera.usermanagement.lib.domain.model.OauthToken;
import com.iddera.usermanagement.lib.domain.model.UserModel;
import com.iddera.usermanagement.lib.domain.model.UserType;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.Base64.getEncoder;

@RequiredArgsConstructor
public class Users {
    private final UserClient userClient;
    private static final String BEARER = "Bearer ";


    public CompletableFuture<OauthToken> login(@NonNull String clientId,
                                               @NonNull String clientSecret,
                                               @NonNull LoginModel loginModel) {

        String basicHeaderAuth = generateBasicAuth(clientId, clientSecret);
        return userClient.login(basicHeaderAuth,
                "password",
                loginModel.getUsername(),
                loginModel.getPassword());
    }

    public CompletableFuture<OauthToken> refreshToken(@NonNull String clientId,
                                                      @NonNull String clientSecret,
                                                      @NonNull String refreshToken) {

        String basicHeaderAuth = generateBasicAuth(clientId, clientSecret);
        return userClient.refreshToken(basicHeaderAuth,
                "refresh_token",
                refreshToken);
    }

    public CompletableFuture<ResponseModel<EmailValidationResponse>> validateEmail(@NonNull EmailModel emailModel) {
        return userClient.validateEmail(emailModel);
    }

    public CompletableFuture<ResponseModel<UserModel>> create(@NonNull UserRequest request) {
        return userClient.create(request)
                .handleAsync(ErrorHandler::handleException);
    }

    public CompletableFuture<ResponseModel<UserModel>> getById(@NonNull Long id,
                                                               @NonNull String token) {
        String bearerToken = bearerToken(token);
        return userClient.getById(id, bearerToken)
                .handleAsync(ErrorHandler::handleException);
    }

    public CompletableFuture<ResponseModel<UserModel>> getByUsername(@NonNull String username,
                                                                     @NonNull String token) {
        String bearerToken = bearerToken(token);
        return userClient.getByUsername(username, bearerToken)
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

    public CompletableFuture<ResponseModel<UserModel>> initiateResetPassword(@NonNull EmailModel request) {
        return userClient.initiateResetPassword(request)
                .handleAsync(ErrorHandler::handleException);
    }

    public CompletableFuture<ResponseModel<UserModel>> resetPassword(@NonNull ForgotPasswordRequest request) {
        return userClient.resetPassword(request)
                .handleAsync(ErrorHandler::handleException);
    }

    public CompletableFuture<ResponseModel<Page<UserModel>>> getAll(Long pageNumber,
                                                                    Long pageSize,
                                                                    UserType userType,
                                                                    @NonNull String token) {
        String bearerToken = bearerToken(token);
        return userClient.getAll(pageNumber, pageSize, userType, bearerToken)
                .handleAsync(ErrorHandler::handleException);
    }

    public CompletableFuture<ResponseModel<List<UserModel>>> getByIds(@NonNull UserSearch userSearch,
                                                                      @NonNull String token) {
        String bearerToken = bearerToken(token);
        return userClient.getByIds(userSearch, bearerToken)
                .handleAsync(ErrorHandler::handleException);
    }

    public CompletableFuture<ResponseModel<String>> createOrUpdatePin(@NonNull PinUpdate pinUpdate,
                                                                      @NonNull String token) {
        String bearerToken = bearerToken(token);
        return userClient.createOrUpdatePIN(pinUpdate, bearerToken)
                .handleAsync(ErrorHandler::handleException);
    }

    public CompletableFuture<ResponseModel<UserModel>> getUserDetails(@NonNull String token) {
        String bearerToken = bearerToken(token);
        return userClient.getUserDetails(bearerToken)
                .handleAsync(ErrorHandler::handleException);
    }

    private String generateBasicAuth(@NonNull String clientId, @NonNull String clientSecret) {
        String clientAuthDetails = clientId + ":" + clientSecret;
        String basicAuth = getEncoder().encodeToString(clientAuthDetails.getBytes());

        return "Basic " + basicAuth;
    }

    private String bearerToken(String token) {
        return BEARER.concat(token);
    }
}

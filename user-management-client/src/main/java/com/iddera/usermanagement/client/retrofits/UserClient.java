package com.iddera.usermanagement.client.retrofits;

import com.iddera.client.model.Page;
import com.iddera.client.model.ResponseModel;
import com.iddera.usermanagement.lib.app.request.*;
import com.iddera.usermanagement.lib.app.response.EmailValidationResponse;
import com.iddera.usermanagement.lib.domain.model.OauthToken;
import com.iddera.usermanagement.lib.domain.model.UserModel;
import com.iddera.usermanagement.lib.domain.model.UserType;
import retrofit2.http.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface UserClient {
    @POST("users")
    CompletableFuture<ResponseModel<UserModel>> create(@Body UserRequest request);

    @FormUrlEncoded
    @POST("oauth/token")
    CompletableFuture<OauthToken> login(@Header("Authorization") String basicAuthorization,
                                        @Field("grant_type") String grantType,
                                        @Field("username") String username,
                                        @Field("password") String password);

    @FormUrlEncoded
    @POST("oauth/token")
    CompletableFuture<OauthToken> refreshToken(@Header("Authorization") String basicAuthorization,
                                               @Field("grant_type") String grantType,
                                               @Field("refresh_token") String refreshToken);

    @GET("users/{id}")
    CompletableFuture<ResponseModel<UserModel>> getById(@Path("id") Long id,
                                                        @Header("Authorization") String bearerToken);

    @POST("users/{id}/change-password")
    CompletableFuture<ResponseModel<UserModel>> changePassword(@Path("id") Long id, @Body ChangeUserPasswordRequest request);

    @GET("users/usernames/{username}")
    CompletableFuture<ResponseModel<UserModel>> getByUsername(@Path("username") String username,
                                                              @Header("Authorization") String bearerToken);

    @POST("users/verify-email")
    CompletableFuture<ResponseModel<UserModel>> verifyEmail(@Body UserVerificationRequest request);

    @POST("users/reset-password/initiate")
    CompletableFuture<ResponseModel<UserModel>> initiateResetPassword(@Body EmailModel request);

    @POST("users/reset-password/update")
    CompletableFuture<ResponseModel<UserModel>> resetPassword(@Body ForgotPasswordRequest request);

    @GET("users/")
    CompletableFuture<ResponseModel<Page<UserModel>>> getAll(@Query("page") Long pageNumber,
                                                             @Query("size") Long pageSize,
                                                             @Query("userType") UserType userType,
                                                             @Header("Authorization") String bearerToken);

    @GET("users/current")
    CompletableFuture<ResponseModel<UserModel>> getUserDetails(@Header("Authorization") String bearerToken);

    @POST("users/searches")
    CompletableFuture<ResponseModel<List<UserModel>>> getByIds(@Body UserSearch userSearch,
                                                               @Header("Authorization") String bearerToken);

    @POST("users/validate-email")
    CompletableFuture<ResponseModel<EmailValidationResponse>> validateEmail(@Body EmailModel emailModel);

    @PUT("users/pin")
    CompletableFuture<ResponseModel<String>> createOrUpdatePIN(@Body PinUpdate pinUpdate,
                                                               @Header("Authorization") String bearerToken);
}

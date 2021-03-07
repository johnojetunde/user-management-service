package com.iddera.usermanagement.client.retrofits;

import com.iddera.client.model.Page;
import com.iddera.client.model.ResponseModel;
import com.iddera.usermanagement.lib.app.request.ChangeUserPasswordRequest;
import com.iddera.usermanagement.lib.app.request.UserRequest;
import com.iddera.usermanagement.lib.app.request.UserVerificationRequest;
import com.iddera.usermanagement.lib.domain.model.UserModel;
import retrofit2.http.*;

import java.util.concurrent.CompletableFuture;

public interface UserClient {
    @POST("users/send-now")
    CompletableFuture<ResponseModel<UserModel>> create(@Body UserRequest request);

    @GET("users/{id}")
    CompletableFuture<ResponseModel<UserModel>> getById(@Path("id") Long id);

    @POST("users/{id}/change-password")
    CompletableFuture<ResponseModel<UserModel>> changePassword(@Path("id") Long id, @Body ChangeUserPasswordRequest request);

    @GET("users/usernames/{username}")
    CompletableFuture<ResponseModel<UserModel>> getByUsername(@Path("username") String username);

    @POST("users/verify-email")
    CompletableFuture<ResponseModel<UserModel>> verifyEmail(@Body UserVerificationRequest request);

    @GET("users/")
    CompletableFuture<ResponseModel<Page<UserModel>>> getAll(@Query("page") Long pageNumber,
                                                             @Query("size") Long pageSize);
}

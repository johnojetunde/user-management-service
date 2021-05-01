package com.iddera.usermanagement.api.app.controller;


import com.iddera.commons.annotation.ValidEnum;
import com.iddera.usermanagement.api.app.model.ResponseModel;
import com.iddera.usermanagement.api.app.model.UserResult;
import com.iddera.usermanagement.api.domain.service.abstracts.UserPasswordService;
import com.iddera.usermanagement.api.domain.service.abstracts.UserService;
import com.iddera.usermanagement.lib.app.request.*;
import com.iddera.usermanagement.lib.domain.model.UserModel;
import com.iddera.usermanagement.lib.domain.model.UserType;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/users", produces = APPLICATION_JSON_VALUE)
public class UserController {

    private final UserService userService;
    private final UserPasswordService userPasswordService;

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    @ApiResponses({@ApiResponse(code = 200, message = "Success", response = UserModel.class)})
    public CompletableFuture<ResponseModel> create(@Valid @RequestBody UserRequest request, Locale locale) {
        return userService.create(request, locale)
                .thenApply(ResponseModel::new);
    }

    @GetMapping("/{id}")
    @ApiResponses({@ApiResponse(code = 200, message = "Success", response = UserModel.class)})
    public CompletableFuture<ResponseModel> get(@PathVariable Long id) {
        return userService.getById(id)
                .thenApply(ResponseModel::new);
    }

    @PostMapping("/{id}/change-password")
    @ApiResponses({@ApiResponse(code = 200, message = "Success", response = UserModel.class)})
    public CompletableFuture<ResponseModel> changePassword(@PathVariable Long id,
                                                           @Valid @RequestBody ChangeUserPasswordRequest changeUserPasswordRequest) {
        return userPasswordService.changePassword(id, changeUserPasswordRequest)
                .thenApply(ResponseModel::new);
    }

    @PostMapping("/reset-password/initiate")
    @ApiResponses({@ApiResponse(code = 200, message = "Success", response = UserModel.class)})
    public CompletableFuture<ResponseModel> initiateForgotPassword(@Valid @RequestBody EmailModel emailModel, Locale locale) {
        return userPasswordService.initiatePasswordReset(emailModel, locale)
                .thenApply(ResponseModel::new);
    }

    @PostMapping("/reset-password/update")
    @ApiResponses({@ApiResponse(code = 200, message = "Success", response = UserModel.class)})
    public CompletableFuture<ResponseModel> resetPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest,
                                                          Locale locale) {
        return userPasswordService.resetPassword(forgotPasswordRequest, locale)
                .thenApply(ResponseModel::new);
    }

    @GetMapping("/usernames/{username}")
    @ApiResponses({@ApiResponse(code = 200, message = "Success", response = UserModel.class)})
    public CompletableFuture<ResponseModel> getByUsername(@PathVariable String username) {
        return userService.getByUserName(username)
                .thenApply(ResponseModel::new);
    }

    @PostMapping("/verify-email")
    @ApiResponses({@ApiResponse(code = 200, message = "Success", response = UserModel.class)})
    public CompletableFuture<ResponseModel> verifyEmail(@Valid @RequestBody UserVerificationRequest userVerificationRequest) {
        return userService.verifyUser(userVerificationRequest)
                .thenApply(ResponseModel::new);
    }

    @GetMapping
    @ApiResponses({@ApiResponse(code = 200, message = "Success", response = UserResult.class)})
    public CompletableFuture<ResponseModel> getAll(@PageableDefault Pageable pageable,
                                                   @Valid @ValidEnum @RequestParam(value = "userType", required = false) UserType userType) {
        return userService.getAll(userType, pageable)
                .thenApply(UserResult::new)
                .thenApply(ResponseModel::new);
    }

    @GetMapping("/current")
    @ApiResponses({@ApiResponse(code = 200, message = "Success", response = UserModel.class)})
    public CompletableFuture<ResponseModel> getUserDetails(Principal principal) {
        return userService.getUserDetails(principal)
                .thenApply(ResponseModel::new);
    }
}

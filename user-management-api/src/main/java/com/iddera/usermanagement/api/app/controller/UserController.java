package com.iddera.usermanagement.api.app.controller;


import com.iddera.usermanagement.api.app.model.ResponseModel;
import com.iddera.usermanagement.api.app.model.UserResult;
import com.iddera.usermanagement.api.domain.service.abstracts.UserService;
import com.iddera.usermanagement.lib.app.request.ChangeUserPasswordRequest;
import com.iddera.usermanagement.lib.app.request.UserRequest;
import com.iddera.usermanagement.lib.app.request.UserVerificationRequest;
import com.iddera.usermanagement.lib.domain.model.UserModel;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.concurrent.CompletableFuture;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/users", produces = APPLICATION_JSON_VALUE)
public class UserController {

    private final UserService userService;

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    @ApiResponses({@ApiResponse(code = 200, message = "Success", response = UserModel.class)})
    public CompletableFuture<ResponseModel> create(@Valid @RequestBody UserRequest request) {
        return userService.create(request)
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
    public CompletableFuture<ResponseModel> changePassword(@PathVariable Long id, @Valid @RequestBody ChangeUserPasswordRequest changeUserPasswordRequest) {
        return userService.changePassword(id,changeUserPasswordRequest)
                .thenApply(ResponseModel::new);
    }

    @GetMapping("/usernames/{username}")
    @ApiResponses({@ApiResponse(code = 200, message = "Success", response = UserModel.class)})
    public CompletableFuture<ResponseModel> get(@PathVariable String username) {
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
    public CompletableFuture<ResponseModel> get(@PageableDefault Pageable pageable) {
        return userService.getAll(pageable)
                .thenApply(UserResult::new)
                .thenApply(ResponseModel::new);
    }

    @GetMapping("/userdetails")
    @ApiResponses({@ApiResponse(code = 200, message = "Success", response = UserModel.class)})
    public CompletableFuture<ResponseModel> getUserDetails(Principal principal) {
        return userService.getUserDetails(principal)
                .thenApply(ResponseModel::new);
    }
}

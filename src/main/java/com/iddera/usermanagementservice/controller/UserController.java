package com.iddera.usermanagementservice.controller;


import com.iddera.usermanagementservice.model.ResponseModel;
import com.iddera.usermanagementservice.model.UserModel;
import com.iddera.usermanagementservice.model.UserResult;
import com.iddera.usermanagementservice.request.ChangeUserPasswordRequest;
import com.iddera.usermanagementservice.request.UserRequest;
import com.iddera.usermanagementservice.request.UserVerificationRequest;
import com.iddera.usermanagementservice.service.abstracts.UserService;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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

    @GetMapping("/by-username/{username}")
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
}

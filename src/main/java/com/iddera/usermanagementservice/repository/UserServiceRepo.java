package com.iddera.usermanagementservice.repository;


import com.iddera.usermanagementservice.entity.User;
import com.iddera.usermanagementservice.request.UserRequest;

import java.util.concurrent.CompletableFuture;

public interface UserServiceRepo {

    CompletableFuture<User> createEntity(UserRequest request);
}

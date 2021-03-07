package com.iddera.usermanagement.api.persistence.repository;


import com.iddera.usermanagement.api.persistence.entity.User;
import com.iddera.usermanagement.lib.app.request.UserRequest;

import java.util.concurrent.CompletableFuture;

public interface UserServiceRepo {

    CompletableFuture<User> createEntity(UserRequest request);
}

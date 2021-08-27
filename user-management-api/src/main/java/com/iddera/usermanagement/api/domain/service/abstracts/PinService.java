package com.iddera.usermanagement.api.domain.service.abstracts;

import com.iddera.usermanagement.lib.app.request.PinUpdate;

import java.util.concurrent.CompletableFuture;

public interface PinService {
    CompletableFuture<String> createOrUpdatePin(PinUpdate pinRequest, String username);
}

package com.iddera.usermanagement.api.domain.service.concretes;

import com.iddera.usermanagement.api.domain.service.abstracts.TokenGenerationService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UuidTokenGenerationService implements TokenGenerationService {
    @Override
    public String generateToken() {
        UUID uuid = UUID.randomUUID();

        return uuid.toString();
    }
}

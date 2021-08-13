package com.iddera.usermanagement.api.domain.service.concretes;

import com.iddera.usermanagement.api.domain.service.abstracts.TokenGenerationService;
import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class DefaultTokenGenerationService implements TokenGenerationService {
    private static Integer TOKEN_BOUND = 999999;

    @Override
    public String generateToken() {
        Random rnd = new Random();
        return String.format("%06d",rnd.nextInt(TOKEN_BOUND));
    }
}

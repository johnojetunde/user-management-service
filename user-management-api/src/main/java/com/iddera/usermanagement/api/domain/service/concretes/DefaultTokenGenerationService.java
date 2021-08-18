package com.iddera.usermanagement.api.domain.service.concretes;

import com.iddera.usermanagement.api.domain.exception.UserManagementException;
import com.iddera.usermanagement.api.domain.service.abstracts.TokenGenerationService;
import com.iddera.usermanagement.api.persistence.repository.RoleRepository;
import com.iddera.usermanagement.api.persistence.repository.UserRepository;
import com.iddera.usermanagement.api.persistence.repository.redis.UserActivationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultTokenGenerationService implements TokenGenerationService {
    private static final Integer TOKEN_LENGTH = 6;
    private static final Integer TOKEN_BOUND = 9;

    private final UserActivationTokenRepository userActivationTokenRepository;
    private final UserRepository userRepository;

    @Value("${max.token.generation.retry.count:10}")
    private int maxRetryCount;

    @Override
    public String generateToken(String username) {
        validateUserExists(username);
        deleteOldTokenIfExists(username);
        return generateToken();
    }

    private String generateToken(){
        int tryCount = 0;
        Optional<String> validToken = Optional.empty();

        while(tryCount < maxRetryCount){
            String generatedToken = generateToken(TOKEN_LENGTH,TOKEN_BOUND);
            if(tokenExists(generatedToken)){
                tryCount++;
                continue;
            }
            validToken = Optional.of(generatedToken);
            break;
        }

        return validToken.orElseThrow(() ->
                new UserManagementException("Maximum token generation retry exceeded, please contact administrator."));
    }

    private String generateToken(int tokenLength, int bound){
        Random rnd = new Random();
        StringBuilder generatedToken = new StringBuilder();
        while(generatedToken.length() < tokenLength){
            generatedToken.append(rnd.nextInt(bound));
        }
        return generatedToken.toString();
    }

    private boolean tokenExists(String token){
        return userActivationTokenRepository.existsByActivationToken(token);
    }

    private void deleteOldTokenIfExists(String username) {
        userActivationTokenRepository.deleteByUsername(username);
    }
    private void validateUserExists(String username){
        boolean userExists = userRepository.existsByUsername(username);
        if(!userExists){
            throw new UsernameNotFoundException(format("User with name: %s does not exist.",username));
        }
    }
}

package com.iddera.usermanagement.api.domain.service.concretes;

import com.iddera.usermanagement.api.domain.exception.UserManagementException;
import com.iddera.usermanagement.api.domain.exception.UserManagementExceptionService;
import com.iddera.usermanagement.api.domain.service.abstracts.TokenGenerationService;
import com.iddera.usermanagement.api.persistence.entity.UserActivationToken;
import com.iddera.usermanagement.api.persistence.repository.UserRepository;
import com.iddera.usermanagement.api.persistence.repository.redis.UserActivationTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

import static java.lang.String.format;

@Service
@Slf4j
public class DefaultTokenGenerationService implements TokenGenerationService {
    private static final Integer TOKEN_LENGTH = 6;
    private static final Integer TOKEN_BOUND = 9;

    private final UserActivationTokenRepository userActivationTokenRepository;
    private final UserRepository userRepository;
    private final Integer maxRetryCount;
    private final UserManagementExceptionService exceptions;

    public DefaultTokenGenerationService(UserActivationTokenRepository userActivationTokenRepository, UserRepository userRepository
            ,@Value("${max.token-generation-retry.count:10}") Integer maxRetryCount, UserManagementExceptionService exceptionService){
        this.userActivationTokenRepository = userActivationTokenRepository;
        this.userRepository = userRepository;
        this.maxRetryCount = maxRetryCount;
        this.exceptions = exceptionService;

    }

    @Override
    public synchronized String generateToken(String username) {
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
        return Optional.ofNullable(userActivationTokenRepository.findByActivationToken(token)).isPresent();
    }

    private void deleteOldTokenIfExists(String username) {
        Optional<UserActivationToken> userActivationToken = userActivationTokenRepository.findByUsername(username);
        userActivationToken.ifPresent(userActivationTokenRepository::delete);
    }

    private void validateUserExists(String username){
        boolean userExists = userRepository.existsByUsername(username);
        if(!userExists){
            throw exceptions.handleCreateBadRequest(format("User with name: %s does not exist.",username));
        }
    }
}

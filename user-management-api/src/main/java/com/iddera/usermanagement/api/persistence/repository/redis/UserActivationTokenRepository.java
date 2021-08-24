package com.iddera.usermanagement.api.persistence.repository.redis;

import com.iddera.usermanagement.api.persistence.entity.UserActivationToken;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserActivationTokenRepository extends CrudRepository<UserActivationToken, Long> {
    Optional<UserActivationToken> findByActivationToken(String token);
    Optional<UserActivationToken> findByUsername(String username);
}
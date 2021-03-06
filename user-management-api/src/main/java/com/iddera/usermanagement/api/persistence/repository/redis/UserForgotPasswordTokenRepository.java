package com.iddera.usermanagement.api.persistence.repository.redis;

import com.iddera.usermanagement.api.persistence.entity.UserForgotPasswordToken;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserForgotPasswordTokenRepository extends CrudRepository<UserForgotPasswordToken, Long> {
    Optional<UserForgotPasswordToken> findByActivationToken(String token);
}
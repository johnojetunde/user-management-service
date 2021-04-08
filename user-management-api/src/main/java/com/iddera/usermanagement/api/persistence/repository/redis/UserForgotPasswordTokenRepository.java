package com.iddera.usermanagement.api.persistence.repository.redis;

import com.iddera.usermanagement.api.persistence.entity.UserForgotPasswordToken;
import org.springframework.data.repository.CrudRepository;

public interface UserForgotPasswordTokenRepository extends CrudRepository<UserForgotPasswordToken,Long> {
    UserForgotPasswordToken findUserForgotPasswordTokenByActivationTokenAndUsername(String token,String username);
    UserForgotPasswordToken findByActivationToken(String token);
    UserForgotPasswordToken findByUsername(String username);
}
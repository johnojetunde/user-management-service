package com.iddera.usermanagement.api.persistence.repository.redis;

import com.iddera.usermanagement.api.persistence.entity.UserActivationToken;
import org.springframework.data.repository.CrudRepository;

public interface UserActivationTokenRepository extends CrudRepository<UserActivationToken,Long> {
    UserActivationToken findUserActivationTokenByUsernameAndActivationToken(String username,String token);
    UserActivationToken findByActivationToken(String token);
    UserActivationToken findByUsername(String username);
}
package com.iddera.usermanagementservice.repository.redis;

import com.iddera.usermanagementservice.entity.redis.UserActivationToken;
import org.springframework.data.repository.CrudRepository;

public interface UserActivationTokenRepository extends CrudRepository<UserActivationToken,Long> {
    UserActivationToken findUserActivationTokenByUsernameAndActivationToken(String username,String token);
    UserActivationToken findByActivationToken(String token);
    UserActivationToken findByUsername(String username);
}
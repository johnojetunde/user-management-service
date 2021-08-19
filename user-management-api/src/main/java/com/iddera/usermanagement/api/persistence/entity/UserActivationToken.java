package com.iddera.usermanagement.api.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

@RedisHash(value = "UserActivationToken", timeToLive = 1800)
@Data
@NoArgsConstructor
@Accessors(chain = true)
@AllArgsConstructor
public class UserActivationToken implements Serializable {
    @Id
    private Long id;

    @Indexed
    private String username;

    @Indexed
    @Column(unique = true)
    private String activationToken;
}
package com.iddera.usermanagement.api.app.config.security;

import com.iddera.usermanagement.api.persistence.entity.User;
import com.iddera.usermanagement.api.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.stream.Collectors;

import static com.iddera.commons.utils.FunctionUtil.emptyIfNullStream;
import static java.lang.String.format;

@RequiredArgsConstructor
public abstract class AuthUserService {

    final UserRepository userRepository;

    User loadUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(format("User %s does not exist", username)));
    }

    Collection<? extends GrantedAuthority> extractRoles(User user) {
        return emptyIfNullStream(user.getRoles())
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }
}

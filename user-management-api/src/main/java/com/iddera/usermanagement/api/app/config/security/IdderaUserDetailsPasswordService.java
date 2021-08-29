package com.iddera.usermanagement.api.app.config.security;

import com.iddera.usermanagement.api.persistence.repository.UserRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Primary
@Component
public class IdderaUserDetailsPasswordService extends AuthUserService implements UserDetailsService {

    public IdderaUserDetailsPasswordService(UserRepository userRepository) {
        super(userRepository);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = loadUser(username);
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                extractRoles(user));
    }
}

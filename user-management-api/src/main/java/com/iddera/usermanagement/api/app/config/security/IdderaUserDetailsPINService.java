package com.iddera.usermanagement.api.app.config.security;

import com.iddera.usermanagement.api.persistence.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class IdderaUserDetailsPINService extends AuthUserService implements UserDetailsService {

    public IdderaUserDetailsPINService(UserRepository userRepository) {
        super(userRepository);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = loadUser(username);
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPin(),
                extractRoles(user));
    }
}

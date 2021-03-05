package com.iddera.usermanagementservice.config;

import com.iddera.usermanagementservice.model.UserType;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Optional<String> loggedInUser = Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(this::getUsername);

        return Optional.of(loggedInUser.orElse(UserType.SYSTEM.toString()));
    }

    private String getUsername(Authentication authentication) {
        if (authentication.getPrincipal() instanceof String) {
            return (String) authentication.getPrincipal();
        }

        var user = (User) authentication.getPrincipal();
        return user.getUsername();
    }
}

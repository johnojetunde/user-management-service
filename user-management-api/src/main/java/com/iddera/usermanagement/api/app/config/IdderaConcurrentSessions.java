package com.iddera.usermanagement.api.app.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.session.data.redis.RedisOperationsSessionRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;

public class IdderaConcurrentSessions implements SessionAuthenticationStrategy {

    private TokenStore tokenStore;

    private final SessionRegistry sessionRegistry;

    private RedisOperationsSessionRepository redisOperationsSessionRepository;

    private String resourceId;

    public IdderaConcurrentSessions(TokenStore tokenStore, SessionRegistry sessionRegistry, String resourceId) {
        this.tokenStore = tokenStore;
        this.sessionRegistry = sessionRegistry;
        this.resourceId = resourceId;
    }

    @Override
    public void onAuthentication(Authentication authentication, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws SessionAuthenticationException {
        UserDetails userDetailsAdaptor = (UserDetails) authentication.getPrincipal();
        Collection<OAuth2AccessToken>  accessTokens = tokenStore.findTokensByClientIdAndUserName(this.resourceId, userDetailsAdaptor.getUsername());
        if (accessTokens != null && !accessTokens.isEmpty()) {
            for (OAuth2AccessToken accessToken :
                    accessTokens) {
                tokenStore.removeAccessToken(accessToken);
            }
        }

        final List<SessionInformation> expiredSessions = sessionRegistry.getAllSessions(authentication.getPrincipal(), true);
        for (SessionInformation s : expiredSessions) {
            if (s.isExpired()) {
                redisOperationsSessionRepository.delete(s.getSessionId());
            }
        }
    }
}

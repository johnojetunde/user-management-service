package com.iddera.usermanagement.api.app.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

@RequiredArgsConstructor
public class IdderaConcurrentSessions implements SessionAuthenticationStrategy {

    private final TokenStore tokenStore;
    private final String resourceId;

    @Override
    public void onAuthentication(Authentication authentication,
                                 HttpServletRequest httpServletRequest,
                                 HttpServletResponse httpServletResponse) throws SessionAuthenticationException {
        UserDetails userDetailsAdaptor = (UserDetails) authentication.getPrincipal();
        Collection<OAuth2AccessToken> accessTokens = tokenStore.findTokensByClientIdAndUserName(this.resourceId, userDetailsAdaptor.getUsername());
        if (accessTokens != null && !accessTokens.isEmpty()) {
            for (OAuth2AccessToken accessToken :
                    accessTokens) {
                tokenStore.removeAccessToken(accessToken);
            }
        }
    }
}

package com.iddera.usermanagement.api.app.config.security;

import org.springframework.beans.factory.annotation.Value;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;


public class SessionListener implements HttpSessionListener {
    @Value("${session.timeout: 3000}")
    private int sessionTimeOut;

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        event.getSession().setMaxInactiveInterval(sessionTimeOut);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        // No-op
    }
}

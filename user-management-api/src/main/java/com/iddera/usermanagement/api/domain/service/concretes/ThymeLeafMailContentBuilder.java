package com.iddera.usermanagement.api.domain.service.concretes;

import com.iddera.usermanagement.api.app.config.EmailConfiguration;
import com.iddera.usermanagement.api.domain.service.abstracts.MailContentBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class ThymeLeafMailContentBuilder implements MailContentBuilder {

    private TemplateEngine templateEngine;

    private EmailConfiguration emailConfiguration;


    @Override
    public String generateMailContent(Map<String, Object> variables, String template, Locale locale) {
        Context context = new Context(locale, variables);
        return templateEngine.process(template, context);
    }

    @Override
    public Map<String, Object> getForgotPasswordProperties(String token) {
        String title = "Forgot your password?";
        String miniTitle = "Reset your password";
        String username = "";
        String message = "Click the button below to be directed to reset your password";
        String btnText = "Reset";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(emailConfiguration.getUserForgotPasswordUrl());
        stringBuilder.append("?tkn=");
        stringBuilder.append(token);
        Map<String, Object> variableMap = new HashMap<>();
        variableMap.put("activationLink", stringBuilder.toString());
        variableMap.put("username", username);
        variableMap.put("miniTitle", miniTitle);
        variableMap.put("message", message);
        variableMap.put("title", title);
        variableMap.put("btnText", btnText);
        return variableMap;
    }

    @Override
    public Map<String, Object> getActivateUserProperties(String username, String token) {
        String title = "Welcome to Iderra,";
        String miniTitle = "It's awesome to have you on board.";
        String message = "Click the button below to activate yourself";
        String btnText = "Activate";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(emailConfiguration.getUserActivationUrl());
        stringBuilder.append("?tkn=");
        stringBuilder.append(token);
        Map<String, Object> variableMap = new HashMap<>();
        variableMap.put("activationLink", stringBuilder.toString());
        variableMap.put("username", username);
        variableMap.put("miniTitle", miniTitle);
        variableMap.put("title", title);
        variableMap.put("message", message);
        variableMap.put("btnText", btnText);
        return variableMap;
    }

    @Autowired
    public ThymeLeafMailContentBuilder(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Autowired
    public void setEmailConfiguration(EmailConfiguration emailConfiguration) {
        this.emailConfiguration = emailConfiguration;
    }
}
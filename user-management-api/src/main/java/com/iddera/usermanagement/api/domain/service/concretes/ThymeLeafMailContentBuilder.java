package com.iddera.usermanagement.api.domain.service.concretes;

import com.iddera.usermanagement.api.domain.service.abstracts.MailContentBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ThymeLeafMailContentBuilder implements MailContentBuilder {

    private final TemplateEngine templateEngine;

    @Override
    public String generateMailContent(Map<String, Object> variables, String template, Locale locale) {
        Context context = new Context(locale, variables);
        return templateEngine.process(template, context);
    }
}
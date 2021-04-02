package com.iddera.usermanagement.api.domain.service.abstracts;

import java.util.Locale;
import java.util.Map;

public interface MailContentBuilder {
     String generateMailContent(Map<String,Object> variables, String template, Locale locale);

     Map<String,Object> getForgotPasswordProperties(String token);

     Map<String,Object> getActivateUserProperties(String username,String token);
}

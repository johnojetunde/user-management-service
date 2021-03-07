package com.iddera.usermanagement.api.domain.service.abstracts;

public interface EmailService {
    boolean sendEmailToOneAddress(String body,String subject,String recipient,String sender);
}

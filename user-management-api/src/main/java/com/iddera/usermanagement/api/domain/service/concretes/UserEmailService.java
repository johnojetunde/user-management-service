package com.iddera.usermanagement.api.domain.service.concretes;

import com.iddera.usermanagement.api.domain.service.abstracts.EmailService;
import com.iddera.usermanagement.api.persistence.entity.User;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.StringWriter;

@Service
public class UserEmailService {

    public static final String PIN_UPDATE = "Iddera PIN UPDATE Notification";

    private final EmailService emailClient;
    private final VelocityEngine velocityEngine;
    private final String emailSender;

    public UserEmailService(EmailService emailClient,
                            VelocityEngine velocityEngine,
                            @Value("${email.default.sender:hello@iddera.com}") String emailSender) {
        this.emailClient = emailClient;
        this.velocityEngine = velocityEngine;
        this.emailSender = emailSender;
    }

    public void sendPinNotification(User user) {
        Template template = velocityEngine.getTemplate("/templates/pinupdate.vm");
        VelocityContext context = new VelocityContext();
        context.put("name", user.getFirstName());
        StringWriter stringWriter = new StringWriter();
        template.merge(context, stringWriter);

        emailClient.sendEmailToOneAddress(
                stringWriter.toString(),
                PIN_UPDATE,
                user.getEmail(), emailSender);
    }
}

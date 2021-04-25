package com.iddera.usermanagement.api.domain.service.concretes;

import com.iddera.client.model.ResponseModel;
import com.iddera.notification.client.NotificationClient;
import com.iddera.notification.client.endpoints.Emails;
import com.iddera.notification.lib.app.EmailRequestModel;
import com.iddera.notification.lib.app.EmailResponseModel;
import com.iddera.notification.lib.domain.EmailBodyType;
import com.iddera.usermanagement.api.domain.service.abstracts.EmailService;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
public class DefaultEmailService implements EmailService {

    private final Emails emailClient;

    public DefaultEmailService(NotificationClient notificationClient) {
        this.emailClient = notificationClient.emails();
    }

    @Override
    public boolean sendEmailToOneAddress(String body, String subject, String recipient, String sender) {
        UUID uuid = UUID.randomUUID();
        var request = EmailRequestModel.builder()
                .body(body)
                .bodyType(EmailBodyType.HTML)
                .sender(sender)
                .referenceId(uuid.toString())
                .subject(subject)
                .recipients(Set.of(recipient))
                .build();
        ResponseModel<EmailResponseModel> response = emailClient.sendNow(request).join();
        return response.isSuccessful();
    }
}

package com.iddera.usermanagementservice.service.concretes;

import com.iddera.client.model.ResponseModel;
import com.iddera.client.provider.RetrofitProvider;
import com.iddera.notification.client.NotificationClient;
import com.iddera.notification.lib.app.EmailRequestModel;
import com.iddera.notification.lib.app.EmailResponseModel;
import com.iddera.notification.lib.domain.EmailBodyType;
import com.iddera.usermanagementservice.service.abstracts.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DefaultEmailService implements EmailService {


    @Value("${notification-client-url: https://iddera-notification-api.herokuapp.com/}")
    private String notificationClientUrl;
    @Override
    public boolean sendEmailToOneAddress(String body, String subject, String recipient, String sender) {
        UUID uuid = UUID.randomUUID();
        RetrofitProvider retrofitProvider = new RetrofitProvider(300, 30, 30);
        NotificationClient notificationClient = new NotificationClient(retrofitProvider, notificationClientUrl);
        var request = EmailRequestModel.builder()
                .body(body)
                .bodyType(EmailBodyType.TEXT)
                .sender(sender)
                .referenceId(uuid.toString())
                .subject(subject)
                .recipients(Set.of(recipient))
                .build();
        ResponseModel<EmailResponseModel> response = notificationClient.emails().sendNow(request).join();
        return response.isSuccessful();
    }


}

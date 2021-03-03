package com.iddera.usermanagementservice.config;

import com.iddera.client.provider.RetrofitProvider;
import com.iddera.notification.client.NotificationClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfigClass {

    private String notificationClientUrl;

    public ClientConfigClass(@Value("${notification-client-url}") String notificationClientUrl) {
        this.notificationClientUrl = notificationClientUrl;
    }

    @Bean
    public NotificationClient notificationClient() {
        RetrofitProvider retrofitProvider = new RetrofitProvider(300, 30, 30);
        return new NotificationClient(retrofitProvider, notificationClientUrl);
    }
}

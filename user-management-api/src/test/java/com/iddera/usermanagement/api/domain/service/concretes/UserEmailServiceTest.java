package com.iddera.usermanagement.api.domain.service.concretes;

import com.iddera.usermanagement.api.domain.service.abstracts.EmailService;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.iddera.usermanagement.api.domain.service.concretes.TestDataFixtures.mockUser;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserEmailServiceTest {
    @Mock
    private EmailService emailClient;
    @Mock
    private VelocityEngine velocityEngine;
    private UserEmailService userEmailService;

    @BeforeEach
    void setUp() {
        userEmailService = new UserEmailService(emailClient, velocityEngine, "hi@iddera.com");
    }

    @Test
    void sendPinUpdateNotification() {
        var user = mockUser();

        when(velocityEngine.getTemplate("/templates/pinupdate.vm"))
                .thenReturn(Mockito.mock(Template.class));
        when(emailClient.sendEmailToOneAddress(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(true);

        userEmailService.sendPinNotification(user);

        verify(velocityEngine).getTemplate("/templates/pinupdate.vm");
        verify(emailClient).sendEmailToOneAddress(anyString(), anyString(), anyString(), anyString());
    }
}
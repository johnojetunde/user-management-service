package com.iddera.usermanagement.api.domain.service.concretes;


import com.iddera.usermanagement.api.domain.exception.UserManagementException;
import com.iddera.usermanagement.api.domain.service.abstracts.TokenGenerationService;
import com.iddera.usermanagement.api.persistence.repository.UserRepository;
import com.iddera.usermanagement.api.persistence.repository.redis.UserActivationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

class DefaultTokenGenerationServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserActivationTokenRepository userActivationTokenRepository;
    private TokenGenerationService tokenGenerationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tokenGenerationService = new DefaultTokenGenerationService(userActivationTokenRepository,userRepository,5);
    }

    @Test
    void tokenGenerationFails_whenUserNameDoesNotExist() {
        assertThatExceptionOfType(UsernameNotFoundException.class)
                .isThrownBy(() -> tokenGenerationService.generateToken(anyString()));
    }

    @Test
    void tokenGenerationFails_WhenMaxRetryCountExceeded() {
        when(userRepository.existsByUsername(anyString()))
                .thenReturn(true);
        when(userActivationTokenRepository.existsByActivationToken(anyString()))
                .thenReturn(true);
        assertThatExceptionOfType(UserManagementException.class)
                .isThrownBy(() -> tokenGenerationService.generateToken(anyString()))
                .withMessage("Maximum token generation retry exceeded, please contact administrator.");
    }

    @Test
    void tokenGenerates_Successfully() {
        when(userRepository.existsByUsername(anyString()))
                .thenReturn(true);
        when(userActivationTokenRepository.existsByActivationToken(anyString()))
                .thenReturn(false);

        String token = tokenGenerationService.generateToken(anyString());
        assertEquals(token.length(), 6);
    }
}
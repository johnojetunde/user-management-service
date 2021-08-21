package com.iddera.usermanagement.api.domain.service.concretes;


import com.iddera.usermanagement.api.domain.exception.UserManagementException;
import com.iddera.usermanagement.api.domain.exception.UserManagementExceptionService;
import com.iddera.usermanagement.api.domain.service.abstracts.TokenGenerationService;
import com.iddera.usermanagement.api.persistence.repository.UserRepository;
import com.iddera.usermanagement.api.persistence.repository.redis.UserActivationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

class DefaultTokenGenerationServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserActivationTokenRepository userActivationTokenRepository;
    private TokenGenerationService tokenGenerationService;
    private UserManagementExceptionService exceptions;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        exceptions = new UserManagementExceptionService();
        tokenGenerationService = new DefaultTokenGenerationService(userActivationTokenRepository,userRepository,5,exceptions);
    }

    @Test
    void tokenGenerationFails_whenUserNameDoesNotExist() {
        assertThatThrownBy(() ->tokenGenerationService.generateToken(anyString()))
                .isInstanceOf(UserManagementException.class)
                .hasFieldOrPropertyWithValue("code", BAD_REQUEST.value());
    }

    @Test
    void tokenGenerationFails_whenMaxRetryCountExceeded() {
        when(userRepository.existsByUsername(anyString()))
                .thenReturn(true);
        when(userActivationTokenRepository.existsByActivationToken(anyString()))
                .thenReturn(true);
        assertThatExceptionOfType(UserManagementException.class)
                .isThrownBy(() -> tokenGenerationService.generateToken(anyString()))
                .withMessage("Maximum token generation retry exceeded, please contact administrator.");
    }

    @Test
    void tokenGenerates_successfully() {
        when(userRepository.existsByUsername(anyString()))
                .thenReturn(true);
        when(userActivationTokenRepository.existsByActivationToken(anyString()))
                .thenReturn(false);

        String token = tokenGenerationService.generateToken(anyString());
        assertEquals(token.length(), 6);
    }
}
package com.iddera.usermanagement.api.domain.service.concretes;

import com.iddera.usermanagement.api.domain.exception.UserManagementException;
import com.iddera.usermanagement.api.domain.exception.UserManagementExceptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.iddera.usermanagement.api.domain.service.concretes.TestDataFixtures.mockUser;
import static com.iddera.usermanagement.api.domain.service.concretes.TestDataFixtures.pinUpdate;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@ExtendWith(MockitoExtension.class)
class NewPinServiceTest {
    private final UserManagementExceptionService exception = new UserManagementExceptionService();
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private NewPinService pinService;

    @BeforeEach
    void setUp() {
        pinService = new NewPinService(passwordEncoder, exception);
    }

    @Test
    void useStrategy() {
        var user = mockUser();
        user.setPin(null);

        assertThat(pinService.useStrategy(user)).isTrue();
    }

    @Test
    void dontUseStrategy() {
        var user = mockUser();
        user.setPin("jdhfhfhfhfhfhfhfhfh");

        assertThat(pinService.useStrategy(user)).isFalse();
    }

    @Test
    void validateFieldFails() {
        var user = mockUser();
        user.setPin(null);
        var pinUpdateModel = pinUpdate();
        pinUpdateModel.setPassword(null);

        assertThatThrownBy(() -> pinService.validateField(user, pinUpdateModel))
                .isInstanceOf(UserManagementException.class)
                .hasMessage("Password is required when creating Pin")
                .hasFieldOrPropertyWithValue("code", BAD_REQUEST.value());
    }

    @Test
    void validateField() {
        var user = mockUser();
        user.setPin(null);
        var pinUpdateModel = pinUpdate();
        pinUpdateModel.setPassword("hdhgdhdhhd");

        assertDoesNotThrow(() -> pinService.validateField(user, pinUpdateModel));
    }

    @Test
    void updatePin_whenPasswordDoesNotMatch() {
        when(passwordEncoder.matches("plainPassword", "hashedPassword"))
                .thenReturn(false);

        var user = mockUser();
        user.setPassword("hashedPassword");
        var pinUpdateModel = pinUpdate();
        pinUpdateModel.setPassword("plainPassword");

        assertThatThrownBy(() -> pinService.updatePin(user, pinUpdateModel))
                .isInstanceOf(UserManagementException.class)
                .hasMessage("Invalid password")
                .hasFieldOrPropertyWithValue("code", UNAUTHORIZED.value());

        verify(passwordEncoder).matches("plainPassword", "hashedPassword");
    }

    @Test
    void updatePin_whenPasswordMatch() {
        when(passwordEncoder.matches("plainPassword", "hashedPassword"))
                .thenReturn(true);
        when(passwordEncoder.encode("1234"))
                .thenReturn("HashedPin");

        var user = mockUser();
        user.setPassword("hashedPassword");
        var pinUpdateModel = pinUpdate();
        pinUpdateModel.setPassword("plainPassword");
        pinUpdateModel.setNewPin("1234");

        var updatedUser = pinService.updatePin(user, pinUpdateModel);

        assertThat(updatedUser.getPin()).isEqualTo("HashedPin");
        assertThat(updatedUser.isPinSet()).isTrue();
        verify(passwordEncoder).matches("plainPassword", "hashedPassword");
        verify(passwordEncoder).encode("1234");
    }
}
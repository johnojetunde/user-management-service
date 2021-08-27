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
class UpdatePinServiceTest {
    private final UserManagementExceptionService exception = new UserManagementExceptionService();
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UpdatePinService pinService;

    @BeforeEach
    void setUp() {
        pinService = new UpdatePinService(passwordEncoder, exception);
    }

    @Test
    void dontUseStrategy() {
        var user = mockUser();
        user.setPin(null);

        assertThat(pinService.useStrategy(user)).isFalse();
    }

    @Test
    void useStrategy() {
        var user = mockUser();
        user.setPin("jdhfhfhfhfhfhfhfhfh");

        assertThat(pinService.useStrategy(user)).isTrue();
    }

    @Test
    void validateFieldFails() {
        var user = mockUser();
        user.setPin("hdhfhgfhfhfh");
        var pinUpdateModel = pinUpdate();
        pinUpdateModel.setCurrentPin(null);
        pinUpdateModel.setPassword(null);

        assertThatThrownBy(() -> pinService.validateField(user, pinUpdateModel))
                .isInstanceOf(UserManagementException.class)
                .hasMessage("Current pin is required")
                .hasFieldOrPropertyWithValue("code", BAD_REQUEST.value());
    }

    @Test
    void validateField() {
        var user = mockUser();
        user.setPin(null);
        var pinUpdateModel = pinUpdate();
        pinUpdateModel.setCurrentPin("1432");
        pinUpdateModel.setPassword(null);

        assertDoesNotThrow(() -> pinService.validateField(user, pinUpdateModel));
    }

    @Test
    void updatePin_whenPinDoesNotMatch() {
        when(passwordEncoder.matches("1432", "hashedCurrentPin"))
                .thenReturn(false);

        var user = mockUser();
        user.setPassword("hashedPassword");
        user.setPin("hashedCurrentPin");
        var pinUpdateModel = pinUpdate();
        pinUpdateModel.setCurrentPin("1432");

        assertThatThrownBy(() -> pinService.updatePin(user, pinUpdateModel))
                .isInstanceOf(UserManagementException.class)
                .hasMessage("Invalid current pin")
                .hasFieldOrPropertyWithValue("code", UNAUTHORIZED.value());

        verify(passwordEncoder).matches("1432", "hashedCurrentPin");
    }

    @Test
    void updatePin_whenPinMatch() {
        when(passwordEncoder.matches("1432", "hashedCurrentPin"))
                .thenReturn(true);
        when(passwordEncoder.encode("1244"))
                .thenReturn("HashedPin");

        var user = mockUser();
        user.setPin("hashedCurrentPin");

        var pinUpdateModel = pinUpdate();
        pinUpdateModel.setPassword(null);
        pinUpdateModel.setNewPin("1244");
        pinUpdateModel.setCurrentPin("1432");

        var updatedUser = pinService.updatePin(user, pinUpdateModel);

        assertThat(updatedUser.getPin()).isEqualTo("HashedPin");
        assertThat(updatedUser.isPinSet()).isTrue();
        verify(passwordEncoder).matches("1432", "hashedCurrentPin");
        verify(passwordEncoder).encode("1244");
    }
}
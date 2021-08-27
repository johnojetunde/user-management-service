package com.iddera.usermanagement.api.domain.service.concretes;

import com.iddera.usermanagement.api.domain.exception.UserManagementException;
import com.iddera.usermanagement.api.domain.exception.UserManagementExceptionService;
import com.iddera.usermanagement.api.persistence.entity.User;
import com.iddera.usermanagement.api.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;

import static com.iddera.usermanagement.api.domain.service.concretes.TestDataFixtures.mockUser;
import static com.iddera.usermanagement.api.domain.service.concretes.TestDataFixtures.pinUpdate;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;
import static org.mockito.quality.Strictness.LENIENT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class UserPinServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private NewPinService newPinService;
    @Mock
    private UpdatePinService updatePinService;
    @Mock
    private UserEmailService userEmailService;
    private UserPinService userPinService;
    private final UserManagementExceptionService exception = new UserManagementExceptionService();

    @BeforeEach
    void setUp() {

        mockPinUpdateServices();

        userPinService = new UserPinService(
                userRepository,
                exception,
                List.of(newPinService, updatePinService),
                userEmailService);
    }

    @Test
    void getPinService_whenUserHasNoPinSet() {
        var user = mockUser();

        var updateService = userPinService.getUpdateService(user);

        assertThat(updateService).isEqualTo(newPinService);
    }

    @Test
    void getPinService_whenUserHasPinSet() {
        var user = mockUser().setPin("1234");

        var updateService = userPinService.getUpdateService(user);

        assertThat(updateService).isEqualTo(updateService);
    }

    @Test
    void getPinServiceFails_whenNoImplementationIsFound() {
        userPinService = new UserPinService(
                userRepository,
                exception,
                emptyList(),
                userEmailService);
        var user = mockUser().setPin("1234");

        assertThatThrownBy(() -> userPinService.getUpdateService(user))
                .isInstanceOf(UserManagementException.class)
                .hasMessage("Error retrieving implementation for PinUpdate")
                .hasFieldOrPropertyWithValue("code", INTERNAL_SERVER_ERROR.value());
    }

    @Test
    void createOrUpdatePinFails_whenUserDoesNotExist() {
        when(userRepository.findByUsername("username"))
                .thenReturn(Optional.empty());

        var pinUpdate = pinUpdate();
        var result = userPinService.createOrUpdatePin(pinUpdate, "username");

        assertThatThrownBy(result::join)
                .isInstanceOf(CompletionException.class)
                .hasCause(new UserManagementException("Invalid user username"))
                .extracting(Throwable::getCause)
                .hasFieldOrPropertyWithValue("code", BAD_REQUEST.value());

        verify(userRepository).findByUsername("username");
    }

    @Test
    void createOrUpdatePin() {
        var pinUpdate = pinUpdate();
        var user = mockUser().setPin(null);

        when(userRepository.findByUsername("username"))
                .thenReturn(Optional.of(user));
        when(newPinService.updateOrCreatePin(isA(User.class), eq(pinUpdate)))
                .then(i -> i.getArgument(0, User.class).setPin(pinUpdate().getCurrentPin()));
        when(userRepository.save(isA(User.class)))
                .then(i -> i.getArgument(0, User.class));
        doNothing()
                .when(userEmailService).sendPinNotification(isA(User.class));

        var result = userPinService.createOrUpdatePin(pinUpdate, "username").join();

        assertThat(result).isEqualTo("PIN Successfully updated");

        verify(userRepository).findByUsername("username");
        verify(newPinService).updateOrCreatePin(isA(User.class), eq(pinUpdate));
        verify(userRepository).save(isA(User.class));
        verify(userEmailService).sendPinNotification(isA(User.class));
    }

    private void mockPinUpdateServices() {
        when(newPinService.useStrategy(isA(User.class)))
                .then(i -> {
                    var user = i.getArgument(0, User.class);
                    return !user.isPinSet();
                });
        when(updatePinService.useStrategy(isA(User.class)))
                .then(i -> {
                    var user = i.getArgument(0, User.class);
                    return user.isPinSet();
                });
    }
}
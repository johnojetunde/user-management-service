package com.iddera.usermanagementservice;


import com.iddera.usermanagementservice.entity.Role;
import com.iddera.usermanagementservice.entity.User;
import com.iddera.usermanagementservice.entity.redis.UserActivationToken;
import com.iddera.usermanagementservice.exception.UserManagementException;
import com.iddera.usermanagementservice.model.Gender;
import com.iddera.usermanagementservice.model.UserModel;
import com.iddera.usermanagementservice.model.UserType;
import com.iddera.usermanagementservice.repository.RoleRepository;
import com.iddera.usermanagementservice.repository.UserRepository;
import com.iddera.usermanagementservice.repository.redis.UserActivationTokenRepository;
import com.iddera.usermanagementservice.request.ChangeUserPasswordRequest;
import com.iddera.usermanagementservice.request.UserRequest;
import com.iddera.usermanagementservice.request.UserUpdateRequest;
import com.iddera.usermanagementservice.service.abstracts.EmailService;
import com.iddera.usermanagementservice.service.abstracts.TokenGenerationService;
import com.iddera.usermanagementservice.service.concretes.DefaultUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class DefaultUserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder encoder;

    @Mock
    private EmailService emailService;
    @Mock
    UserActivationTokenRepository userActivationTokenRepository;
    @Mock
    TokenGenerationService tokenGenerationService;
    private DefaultUserService userService;
    private static final Clock clock = Clock.fixed(
            Instant.parse("2020-12-04T10:15:30.00Z"),
            ZoneId.systemDefault());

    @BeforeEach
    void setUp() {
        userService = new DefaultUserService(
                userRepository,
                roleRepository,
                newFixedThreadPool(3),
                encoder,
                emailService,
                userActivationTokenRepository,
                tokenGenerationService);
    }

    @Test
    void createFails_whenUserNameExists() {
        when(userRepository.existsByUsername(eq("iddera")))
                .thenReturn(true);

        CompletableFuture<UserModel> result = userService.create(buildUserRequest());

        assertThatThrownBy(result::join)
                .isInstanceOf(CompletionException.class)
                .hasCause(new UserManagementException("Username iddera exists"))
                .extracting(Throwable::getCause)
                .hasFieldOrPropertyWithValue("code", BAD_REQUEST.value());
    }

    @Test
    void createFails_whenEmailExists() {
        when(userRepository.existsByEmail(eq("email@email.com")))
                .thenReturn(true);

        CompletableFuture<UserModel> result = userService.create(buildUserRequest());

        assertThatThrownBy(result::join)
                .isInstanceOf(CompletionException.class)
                .hasCause(new UserManagementException("Email email@email.com exists"))
                .extracting(Throwable::getCause)
                .hasFieldOrPropertyWithValue("code", BAD_REQUEST.value());
    }

    @Test
    void createFails_whenRoleNotFound() {
        when(userRepository.existsByUsername(eq("iddera")))
                .thenReturn(false);
        when(userRepository.existsByEmail(eq("email@email.com")))
                .thenReturn(false);
        when(roleRepository.findById(eq(1L)))
                .thenReturn(Optional.empty());

        CompletableFuture<UserModel> result = userService.create(buildUserRequest());

        assertThatThrownBy(result::join)
                .isInstanceOf(CompletionException.class)
                .hasCause(new UserManagementException("Role 1 does not exist"))
                .extracting(Throwable::getCause)
                .hasFieldOrPropertyWithValue("code", BAD_REQUEST.value());
    }

    @Test
    void createSuccessfully() {
        when(userRepository.existsByUsername(eq("iddera")))
                .thenReturn(false);
        when(emailService.sendEmailToOneAddress(anyString(),anyString(),anyString(),anyString()))
                .thenReturn(true);
        when(userRepository.existsByEmail(eq("email@email.com")))
                .thenReturn(false);
        when(roleRepository.findById(eq(1L)))
                .thenReturn(Optional.of(role()));
        when(encoder.encode(eq("iddera")))
                .thenReturn("HashedPassword");
        when(userActivationTokenRepository.save(any(UserActivationToken.class)))
                .then(i -> {
                    var entity = i.getArgument(0,UserActivationToken.class);
                    entity.setId(1009233L);
                    return entity;
                });
        when(userRepository.save(any(User.class)))
                .then(i -> {
                    var entity = i.getArgument(0, User.class);
                    entity.setId(1L);
                    return entity;
                });

        UserModel result = userService.create(buildUserRequest()).join();

        assertUserValues(result);
        verify(userRepository).existsByUsername(eq("iddera"));
        verify(userRepository).existsByEmail(eq("email@email.com"));
        verify(roleRepository).findById(eq(1L));
        verify(encoder).encode(eq("iddera"));
        verify(userRepository).save(any(User.class));
        verify(userActivationTokenRepository).save(any(UserActivationToken.class));
        verify(emailService).sendEmailToOneAddress(anyString(),anyString(),anyString(),anyString()) ;
    }

    @Test
    void updateFails_whenUseDoesNotExists() {
        when(userRepository.findById(eq(1L)))
                .thenReturn(Optional.empty());

        CompletableFuture<UserModel> result = userService.update(1L, buildUserUpdateRequest());

        assertThatThrownBy(result::join)
                .isInstanceOf(CompletionException.class)
                .hasCause(new UserManagementException("User does not exist"))
                .extracting(Throwable::getCause)
                .hasFieldOrPropertyWithValue("code", BAD_REQUEST.value());
    }

    @Test
    void updateFails_whenEmailExists() {
        when(userRepository.findById(eq(1L)))
                .thenReturn(Optional.of(user()));
        when(userRepository.existsUserByIdIsNotAndEmail(eq(1L), eq("email@email.com")))
                .thenReturn(true);

        CompletableFuture<UserModel> result = userService.update(1L, buildUserUpdateRequest());

        assertThatThrownBy(result::join)
                .isInstanceOf(CompletionException.class)
                .hasCause(new UserManagementException("Email email@email.com exists"))
                .extracting(Throwable::getCause)
                .hasFieldOrPropertyWithValue("code", BAD_REQUEST.value());
    }

    @Test
    void updateFails_whenUserNameExists() {
        when(userRepository.findById(eq(1L)))
                .thenReturn(Optional.of(user()));
        when(userRepository.existsUserByIdIsNotAndUsername(eq(1L), eq("iddera")))
                .thenReturn(true);

        CompletableFuture<UserModel> result = userService.update(1L, buildUserUpdateRequest());

        assertThatThrownBy(result::join)
                .isInstanceOf(CompletionException.class)
                .hasCause(new UserManagementException("Username iddera exists"))
                .extracting(Throwable::getCause)
                .hasFieldOrPropertyWithValue("code", BAD_REQUEST.value());
    }

    @Test
    void updateFails_whenRoleNotFound() {
        when(userRepository.findById(eq(1L)))
                .thenReturn(Optional.of(user()));
        when(userRepository.existsUserByIdIsNotAndUsername(eq(1L), eq("iddera")))
                .thenReturn(false);
        when(userRepository.existsUserByIdIsNotAndEmail(eq(1L), eq("email@email.com")))
                .thenReturn(false);
        when(roleRepository.findById(eq(1L)))
                .thenReturn(Optional.empty());

        CompletableFuture<UserModel> result = userService.update(1L, buildUserUpdateRequest());

        assertThatThrownBy(result::join)
                .isInstanceOf(CompletionException.class)
                .hasCause(new UserManagementException("Role 1 does not exist"))
                .extracting(Throwable::getCause)
                .hasFieldOrPropertyWithValue("code", BAD_REQUEST.value());
    }

    @Test
    void updateSuccessfully() {
        when(userRepository.findById(eq(1L)))
                .thenReturn(Optional.of(user()));
        when(userRepository.existsUserByIdIsNotAndUsername(eq(1L), eq("iddera")))
                .thenReturn(false);
        when(userRepository.existsUserByIdIsNotAndEmail(eq(1L), eq("email@email.com")))
                .thenReturn(false);
        when(roleRepository.findById(eq(1L)))
                .thenReturn(Optional.of(role()));
        when(userRepository.save(any(User.class)))
                .then(i -> {
                    var entity = i.getArgument(0, User.class);
                    entity.setId(1L);
                    return entity;
                });

        UserModel result = userService.update(1L, buildUserUpdateRequest()).join();

        assertUserValues(result);
        verify(userRepository).existsUserByIdIsNotAndUsername(eq(1L), eq("iddera"));
        verify(userRepository).existsUserByIdIsNotAndEmail(eq(1L), eq("email@email.com"));
        verify(roleRepository).findById(eq(1L));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getByUserNameFails_whenUserNotFound() {
        when(userRepository.findByUsername(eq("username")))
                .thenReturn(Optional.empty());

        CompletableFuture<UserModel> result = userService.getByUserName("username");

        assertThatThrownBy(result::join)
                .isInstanceOf(CompletionException.class)
                .hasCause(new UserManagementException("User not found"))
                .extracting(Throwable::getCause)
                .hasFieldOrPropertyWithValue("code", NOT_FOUND.value());
    }

    @Test
    void getByUserNamePasses() {
        when(userRepository.findByUsername(eq("username")))
                .thenReturn(Optional.of(user()));

        UserModel result = userService.getByUserName("username").join();
        assertUserValues(result);
        verify(userRepository).findByUsername(eq("username"));
    }


    @Test
    void getAll() {
        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(singletonList(user())));

        Page<UserModel> userModels = userService.getAll(mock(Pageable.class)).join();

        assertThat(userModels.getSize()).isEqualTo(1);
        assertUserValues(userModels.getContent().get(0));
    }

    @Test
    void getByIdFails_whenNotFound() {
        when(userRepository.findById(eq(1L)))
                .thenReturn(Optional.empty());

        CompletableFuture<UserModel> result = userService.getById(1L);

        assertThatThrownBy(result::join)
                .isInstanceOf(CompletionException.class)
                .hasCause(new UserManagementException("User not found"))
                .extracting(Throwable::getCause)
                .hasFieldOrPropertyWithValue("code", NOT_FOUND.value());
    }

    @Test
    void getByIdPasses() {
        when(userRepository.findById(eq(1L)))
                .thenReturn(Optional.of(user()));

        UserModel result = userService.getById(1L).join();
        assertUserValues(result);
        verify(userRepository).findById(eq(1L));
    }

    @Test
    void changePasswordFails_whenUserNotFound()
    {
        when(userRepository.findById(eq(1L)))
                .thenReturn(Optional.empty());

        CompletableFuture<UserModel> result = userService.changePassword(1L,buildChangeUserUpdateRequest());

        assertThatThrownBy(result::join)
                .isInstanceOf(CompletionException.class)
                .hasCause(new UserManagementException("User does not exist"))
                .extracting(Throwable::getCause)
                .hasFieldOrPropertyWithValue("code",NOT_FOUND.value());
        verify(userRepository).findById(eq(1L));
    }

    @Test
    void changePasswordFails_whenUserPasswordNotMatchOldPassword()
    {
        when(userRepository.findById(eq(1L)))
                .thenReturn(Optional.of(user()));
        when(encoder.matches(eq("iderra"),eq("iddera")))
                .thenReturn(false);
        ChangeUserPasswordRequest changeUserPasswordRequest = buildChangeUserUpdateRequest();
        changeUserPasswordRequest.setOldPassword("iderra");

        CompletableFuture<UserModel> result = userService.changePassword(1L,changeUserPasswordRequest);

        assertThatThrownBy(result::join)
                .isInstanceOf(CompletionException.class)
                .hasCause(new UserManagementException("User password doesn't match old password"))
                .extracting(Throwable::getCause)
                .hasFieldOrPropertyWithValue("code",BAD_REQUEST.value());
        verify(userRepository).findById(eq(1L));
        verify(encoder).matches(eq("iderra"),eq("iddera"));

    }

    @Test
    void changePasswordFails_whenUNewPasswordNotMatchConfirmPassword()
    {
        when(userRepository.findById(eq(1L)))
                .thenReturn(Optional.of(user()));
        when(encoder.matches(eq("iddera"),eq("iddera")))
                .thenReturn(true);

        ChangeUserPasswordRequest changeUserPasswordRequest = buildChangeUserUpdateRequest();
        changeUserPasswordRequest.setConfirmPassword("idera");

        CompletableFuture<UserModel> result = userService.changePassword(1L,changeUserPasswordRequest);

        assertThatThrownBy(result::join)
                .isInstanceOf(CompletionException.class)
                .hasCause(new UserManagementException("Password and confirmed password do not match"))
                .extracting(Throwable::getCause)
                .hasFieldOrPropertyWithValue("code",BAD_REQUEST.value());
        verify(userRepository).findById(eq(1L));
        verify(encoder).matches(eq("iddera"),eq("iddera"));


    }
    @Test
    void changePasswordPasses()
    {
        when(userRepository.findById(eq(1L)))
                .thenReturn(Optional.of(user()));
        when(encoder.matches(eq("iddera"),eq("iddera")))
                .thenReturn(true);
        when(encoder.encode(eq("iderra")))
                .thenReturn("HashedIderra");
        when(userRepository.save(any(User.class)))
                .then(i -> {
                    var entity = i.getArgument(0, User.class);
                    entity.setId(1L);
                    return entity;
                });
        when(emailService.sendEmailToOneAddress(anyString(),anyString(),anyString(),anyString()))
                .thenReturn(true);
        ChangeUserPasswordRequest changeUserPasswordRequest = buildChangeUserUpdateRequest();

        UserModel result = userService.changePassword(1L,changeUserPasswordRequest).join();

        assertUserValues(result);
        verify(userRepository).findById(eq(1L));
        verify(encoder).matches(eq("iddera"),eq("iddera"));
        verify(encoder).encode(eq("iderra"));
        verify(userRepository).save(any(User.class));



    }

    private Role role() {
        var role = new Role()
                .setName("ROLE")
                .setDescription("Description");
        role.setId(1L);
        return role;
    }

    private  User user() {
        var user = new User()
                .setFirstName("Firstname")
                .setLastName("Lastname")
                .setUsername("iddera")
                .setEmail("email@email.com")
                .setDateOfBirth(new Date())
                .setType(UserType.ADMIN)
                .setPassword("iddera")
                .setRoles(singletonList(role()));
        user.setId(1L);
        return user;
    }


    private void assertUserValues(UserModel result) {
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("iddera");
        assertThat(result.getEmail()).isEqualTo("email@email.com");
        assertThat(result.getFirstName()).isEqualTo("Firstname");
        assertThat(result.getLastName()).isEqualTo("Lastname");
        assertThat(result.getType()).isEqualTo(UserType.ADMIN);
        assertThat(result.getRole()).hasSize(1)
                .containsExactly(role().toModel());
    }
    private UserRequest buildUserRequest() {
        return new UserRequest()
                .setFirstName("Firstname")
                .setLastName("Lastname")
                .setUsername("iddera")
                .setEmail("email@email.com")
                .setDateOfBirth(new Date())
                .setRoleId(1L)
                .setType(UserType.ADMIN)
                .setPassword("iddera")
                .setConfirmPassword("iddera")
                .setGender(Gender.MALE);
    }

    private UserActivationToken buildUserActivationToken(){
        return new UserActivationToken().setActivationToken("0000-1111-2222-4444")
                .setUsername("iddera");
    }

    private   UserUpdateRequest buildUserUpdateRequest() {
        return new UserUpdateRequest()
                .setFirstName("Firstname")
                .setLastName("Lastname")
                .setUsername("iddera")
                .setEmail("email@email.com")
                .setDateOfBirth(new Date())
                .setRoleId(1L)
                .setType(UserType.ADMIN);
    }

    private ChangeUserPasswordRequest buildChangeUserUpdateRequest(){
        return new ChangeUserPasswordRequest()
                .setOldPassword("iddera")
                .setNewPassword("iderra")
                .setConfirmPassword("iderra");
    }
}
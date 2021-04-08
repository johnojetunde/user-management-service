package com.iddera.usermanagement.api;


import com.iddera.usermanagement.api.app.config.EmailConfiguration;
import com.iddera.usermanagement.api.app.util.Constants;
import com.iddera.usermanagement.api.domain.exception.UserManagementException;
import com.iddera.usermanagement.api.domain.exception.UserManagementExceptionService;
import com.iddera.usermanagement.api.domain.service.abstracts.*;
import com.iddera.usermanagement.api.domain.service.concretes.DefaultUserService;
import com.iddera.usermanagement.api.persistence.entity.Role;
import com.iddera.usermanagement.api.persistence.entity.User;
import com.iddera.usermanagement.api.persistence.entity.UserActivationToken;
import com.iddera.usermanagement.api.persistence.entity.UserForgotPasswordToken;
import com.iddera.usermanagement.api.persistence.repository.RoleRepository;
import com.iddera.usermanagement.api.persistence.repository.UserRepository;
import com.iddera.usermanagement.api.persistence.repository.redis.UserActivationTokenRepository;
import com.iddera.usermanagement.api.persistence.repository.redis.UserForgotPasswordTokenRepository;
import com.iddera.usermanagement.lib.app.request.ChangeUserPasswordRequest;
import com.iddera.usermanagement.lib.app.request.ForgotPasswordRequest;
import com.iddera.usermanagement.lib.app.request.UserRequest;
import com.iddera.usermanagement.lib.app.request.UserUpdateRequest;
import com.iddera.usermanagement.lib.domain.model.Gender;
import com.iddera.usermanagement.lib.domain.model.UserModel;
import com.iddera.usermanagement.lib.domain.model.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static java.util.Collections.singletonList;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

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
    MailContentBuilder mailContentBuilder;
    @Mock
    UserActivationTokenRepository userActivationTokenRepository;

    @Mock
    UserForgotPasswordTokenRepository userForgotPasswordTokenRepository;
    @Mock
    TokenGenerationService tokenGenerationService;

    @Mock
    EmailConfiguration emailConfiguration;
    private DefaultUserService userService;
    private static final Clock clock = Clock.fixed(
            Instant.parse("2020-12-04T10:15:30.00Z"),
            ZoneId.systemDefault());
    @Mock
    UserActivationService userActivationService;

    @Mock
    UserPasswordService userPasswordService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new DefaultUserService(
                userRepository,
                roleRepository,
                newFixedThreadPool(3),
                encoder,
                emailService,
                userActivationTokenRepository,
                tokenGenerationService,
                userForgotPasswordTokenRepository,
                new UserManagementExceptionService(), mailContentBuilder,
                emailConfiguration);
    }

    @Test
    void createFails_whenUserNameExists() {
        when(userRepository.existsByUsername(eq("iddera")))
                .thenReturn(true);

        CompletableFuture<UserModel> result = userService.create(buildUserRequest(),new Locale("en-NG"));

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

        CompletableFuture<UserModel> result = userService.create(buildUserRequest(),new Locale("en-NG") );

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

        CompletableFuture<UserModel> result = userService.create(buildUserRequest(),new Locale("en-NG") );

        assertThatThrownBy(result::join)
                .isInstanceOf(CompletionException.class)
                .hasCause(new UserManagementException("Role 1 does not exist"))
                .extracting(Throwable::getCause)
                .hasFieldOrPropertyWithValue("code", BAD_REQUEST.value());
    }

    @Test
    void createSuccessfully() {
        Locale locale = new Locale("en");
        when(userRepository.existsByUsername(eq("iddera")))
                .thenReturn(false);
        when(emailService.sendEmailToOneAddress(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(true);
        when(userRepository.existsByEmail(eq("email@email.com")))
                .thenReturn(false);
        when(roleRepository.findById(eq(1L)))
                .thenReturn(Optional.of(role()));
        when(encoder.encode(eq("iddera")))
                .thenReturn("HashedPassword");
        when(userActivationTokenRepository.save(any(UserActivationToken.class)))
                .then(i -> {
                    var entity = i.getArgument(0, UserActivationToken.class);
                    entity.setId(1009233L);
                    return entity;
                });
        when(userRepository.save(any(User.class)))
                .then(i -> {
                    var entity = i.getArgument(0, User.class);
                    entity.setId(1L);
                    return entity;
                });
        when(tokenGenerationService.generateToken())
                .thenReturn("123456789");
        when(userActivationTokenRepository.save(any()))
                .thenReturn(buildUserActivationToken());

        when(userActivationService.getActivateUserProperties("iddera", "123456789"))
                .thenReturn(new HashMap<>());
        when(mailContentBuilder.generateMailContent(any(), eq(Constants.TEMPLATE), eq(locale)))
                .thenReturn("New User Email Is Here!!");

        UserModel result = userService.create(buildUserRequest(),locale ).join();

        assertUserValues(result);
        verify(userRepository).existsByUsername(eq("iddera"));
        verify(userRepository).existsByEmail(eq("email@email.com"));
        verify(roleRepository).findById(eq(1L));
        verify(encoder).encode(eq("iddera"));
        verify(userRepository).save(any(User.class));
        verify(userActivationTokenRepository).save(any(UserActivationToken.class));
        verify(emailService).sendEmailToOneAddress(anyString(), anyString(), anyString(), anyString());
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
    void changePasswordFails_whenUserNotFound() {
        when(userRepository.findById(eq(1L)))
                .thenReturn(Optional.empty());

        CompletableFuture<UserModel> result = userService.changePassword(1L, buildChangeUserUpdateRequest());

        assertThatThrownBy(result::join)
                .isInstanceOf(CompletionException.class)
                .hasCause(new UserManagementException("User does not exist"))
                .extracting(Throwable::getCause)
                .hasFieldOrPropertyWithValue("code", NOT_FOUND.value());
        verify(userRepository).findById(eq(1L));
    }

    @Test
    void changePasswordFails_whenUserPasswordNotMatchOldPassword() {
        when(userRepository.findById(eq(1L)))
                .thenReturn(Optional.of(user()));
        when(encoder.matches(eq("iderra"), eq("iddera")))
                .thenReturn(false);
        ChangeUserPasswordRequest changeUserPasswordRequest = buildChangeUserUpdateRequest();
        changeUserPasswordRequest.setOldPassword("iderra");

        CompletableFuture<UserModel> result = userService.changePassword(1L, changeUserPasswordRequest);

        assertThatThrownBy(result::join)
                .isInstanceOf(CompletionException.class)
                .hasCause(new UserManagementException("User password doesn't match old password"))
                .extracting(Throwable::getCause)
                .hasFieldOrPropertyWithValue("code", BAD_REQUEST.value());
        verify(userRepository).findById(eq(1L));
        verify(encoder).matches(eq("iderra"), eq("iddera"));

    }

    @Test
    void changePasswordFails_whenUNewPasswordNotMatchConfirmPassword() {
        when(userRepository.findById(eq(1L)))
                .thenReturn(Optional.of(user()));
        when(encoder.matches(eq("iddera"), eq("iddera")))
                .thenReturn(true);

        ChangeUserPasswordRequest changeUserPasswordRequest = buildChangeUserUpdateRequest();
        changeUserPasswordRequest.setConfirmPassword("idera");

        CompletableFuture<UserModel> result = userService.changePassword(1L, changeUserPasswordRequest);

        assertThatThrownBy(result::join)
                .isInstanceOf(CompletionException.class)
                .hasCause(new UserManagementException("Password and confirmed password do not match"))
                .extracting(Throwable::getCause)
                .hasFieldOrPropertyWithValue("code", BAD_REQUEST.value());
        verify(userRepository).findById(eq(1L));
        verify(encoder).matches(eq("iddera"), eq("iddera"));


    }

    @Test
    void changePasswordPasses() {
        when(userRepository.findById(eq(1L)))
                .thenReturn(Optional.of(user()));
        when(encoder.matches(eq("iddera"), eq("iddera")))
                .thenReturn(true);
        when(encoder.encode(eq("iderra")))
                .thenReturn("HashedIderra");
        when(userRepository.save(any(User.class)))
                .then(i -> {
                    var entity = i.getArgument(0, User.class);
                    entity.setId(1L);
                    return entity;
                });
        when(emailService.sendEmailToOneAddress(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(true);
        ChangeUserPasswordRequest changeUserPasswordRequest = buildChangeUserUpdateRequest();

        UserModel result = userService.changePassword(1L, changeUserPasswordRequest).join();

        assertUserValues(result);
        verify(userRepository).findById(eq(1L));
        verify(encoder).matches(eq("iddera"), eq("iddera"));
        verify(encoder).encode(eq("iderra"));
        verify(userRepository).save(any(User.class));


    }

    @Test
    void forgotPasswordSuccess() {
        Locale locale = new Locale("en");
        when(userRepository.findByUsername(eq("iddera")))
                .thenReturn(Optional.of(user()));
        when(tokenGenerationService.generateToken())
                .thenReturn("123456789");
        when(userActivationTokenRepository.save(any()))
                .thenReturn(buildUserActivationToken());


        when(mailContentBuilder.generateMailContent(any(), eq(Constants.TEMPLATE), eq(locale)))
                .thenReturn("User Password Email Is Here!!");
        UserModel result = userService.forgotPassword("iddera", locale).join();
        assertUserValues(result);
        verify(userRepository).findByUsername("iddera");
        verify(tokenGenerationService).generateToken();
        verify(userActivationTokenRepository).save(any());

        verify(mailContentBuilder).generateMailContent(any(HashMap.class), eq(Constants.TEMPLATE), eq(locale));
    }

    @Test
    void forgotPasswordFailsWrongUsername(){
        when(userRepository.findByUsername(eq("iddera")))
                .thenReturn(Optional.empty());


        CompletableFuture<UserModel> result = userService.forgotPassword("iddera", new Locale("en"));


        assertThatThrownBy(result::join)
                .isInstanceOf(CompletionException.class)
                .hasCause(new UserManagementException("User iddera not found"))
                .extracting(Throwable::getCause)
                .hasFieldOrPropertyWithValue("code", NOT_FOUND.value());
        verify(userRepository).findByUsername(eq("iddera"));
    }
    @Test
    void resetPasswordSuccess(){
        Locale locale = new Locale("en");
        when(userRepository.findById(eq(1L)))
                .thenReturn(Optional.of(user()));
        when(userRepository.save(any()))
                .thenReturn(user());
        when(userForgotPasswordTokenRepository.findByUsername("iddera"))
                .thenReturn(buildUserForgottenPassword());
        UserModel result = userService.resetPassword(1L,buildForgotPasswordRequest(),locale).join();
        assertUserValues(result);
        verify(userRepository).findById(1L);
        verify(userRepository).save(any());
    }
    @Test
    void resetPasswordFailedTokenNotFound(){
        Locale locale = new Locale("en");
        when(userRepository.findById(eq(1L)))
                .thenReturn(Optional.of(user()));

        when(userForgotPasswordTokenRepository.findByUsername("iddera"))
                .thenReturn(null);
        CompletableFuture<UserModel> result = userService.resetPassword(1L,buildForgotPasswordRequest(),locale);

        assertThatThrownBy(result::join)
                .isInstanceOf(CompletionException.class)
                .hasCause(new UserManagementException("User token not found for user iddera"))
                .extracting(Throwable::getCause)
                .hasFieldOrPropertyWithValue("code", NOT_FOUND.value());
        verify(userForgotPasswordTokenRepository).findByUsername(eq("iddera"));
        verify(userRepository).findById(1L);
    }
    @Test
    void resetPasswordFailedTokenNotMatch(){
        Locale locale = new Locale("en");
        when(userRepository.findById(eq(1L)))
                .thenReturn(Optional.of(user()));

        when(userForgotPasswordTokenRepository.findByUsername("iddera"))
                .thenReturn(buildUserForgottenPassword());
        ForgotPasswordRequest forgotPasswordRequest = buildForgotPasswordRequest();
        forgotPasswordRequest.setToken("987654321");
        CompletableFuture<UserModel> result = userService.resetPassword(1L,forgotPasswordRequest,locale);

        assertThatThrownBy(result::join)
                .isInstanceOf(CompletionException.class)
                .hasCause(new UserManagementException("This token isn't mapped to the user"))
                .extracting(Throwable::getCause)
                .hasFieldOrPropertyWithValue("code", BAD_REQUEST.value());
        verify(userForgotPasswordTokenRepository).findByUsername(eq("iddera"));
        verify(userRepository).findById(1L);
    }

    @Test
    void resetPasswordFailsNewPasswordConfirmedPasswordMismatch(){
        when(userRepository.findById(eq(1L)))
                .thenReturn(Optional.of(user()));


        ForgotPasswordRequest forgotPasswordRequest = buildForgotPasswordRequest();
        forgotPasswordRequest.setConfirmPassword("idera");

        CompletableFuture<UserModel> result = userService.resetPassword(1L, forgotPasswordRequest,new Locale("en"));

        assertThatThrownBy(result::join)
                .isInstanceOf(CompletionException.class)
                .hasCause(new UserManagementException("Password and confirmed password do not match"))
                .extracting(Throwable::getCause)
                .hasFieldOrPropertyWithValue("code", BAD_REQUEST.value());
        verify(userRepository).findById(eq(1L));
    }

    @Test
    void resetPasswordFailsWrongUsername(){
        when(userRepository.findById(eq(1L)))
                .thenReturn(Optional.empty());


        CompletableFuture<UserModel> result = userService.resetPassword(1L,buildForgotPasswordRequest(), new Locale("en"));


        assertThatThrownBy(result::join)
                .isInstanceOf(CompletionException.class)
                .hasCause(new UserManagementException("User does not exist"))
                .extracting(Throwable::getCause)
                .hasFieldOrPropertyWithValue("code", NOT_FOUND.value());
        verify(userRepository).findById(eq(1L));
    }

    private Role role() {
        var role = new Role()
                .setName("ROLE")
                .setDescription("Description");
        role.setId(1L);
        return role;
    }

    private User user() {
        var user = new User()
                .setFirstName("Firstname")
                .setLastName("Lastname")
                .setUsername("iddera")
                .setEmail("email@email.com")
                .setDateOfBirth(LocalDate.now())
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
                .setDateOfBirth(LocalDate.now())
                .setRoleId(1L)
                .setType(UserType.ADMIN)
                .setPassword("iddera")
                .setConfirmPassword("iddera")
                .setGender(Gender.MALE);
    }

    private UserUpdateRequest buildUserUpdateRequest() {
        return new UserUpdateRequest()
                .setFirstName("Firstname")
                .setLastName("Lastname")
                .setUsername("iddera")
                .setEmail("email@email.com")
                .setDateOfBirth(LocalDate.now())
                .setRoleId(1L)
                .setType(UserType.ADMIN);
    }

    private ChangeUserPasswordRequest buildChangeUserUpdateRequest() {
        return new ChangeUserPasswordRequest()
                .setOldPassword("iddera")
                .setNewPassword("iderra")
                .setConfirmPassword("iderra");
    }
    private ForgotPasswordRequest buildForgotPasswordRequest() {
        return new ForgotPasswordRequest()
                .setToken("123456789")
                .setNewPassword("iddera")
                .setConfirmPassword("iddera");
    }
    private UserForgotPasswordToken buildUserForgottenPassword(){
        return new UserForgotPasswordToken()
                    .setUsername("iddera")
                    .setActivationToken("123456789")
                    .setId(1L);
    }
    private UserActivationToken buildUserActivationToken(){
        return new UserActivationToken()
                .setActivationToken("123456789")
                .setUsername("iddera")
                .setId(1L);
    }

}
package com.iddera.usermanagement.api.domain.service.concretes;


import com.iddera.commons.utils.ValidationUtil;
import com.iddera.usermanagement.api.app.config.EmailConfiguration;
import com.iddera.usermanagement.api.app.util.Constants;
import com.iddera.usermanagement.api.app.util.TemplateConstants;
import com.iddera.usermanagement.api.domain.exception.UserManagementExceptionService;
import com.iddera.usermanagement.api.domain.service.abstracts.*;
import com.iddera.usermanagement.api.persistence.entity.Role;
import com.iddera.usermanagement.api.persistence.entity.User;
import com.iddera.usermanagement.api.persistence.entity.UserActivationToken;
import com.iddera.usermanagement.api.persistence.entity.UserForgotPasswordToken;
import com.iddera.usermanagement.api.persistence.repository.RoleRepository;
import com.iddera.usermanagement.api.persistence.repository.UserRepository;
import com.iddera.usermanagement.api.persistence.repository.UserServiceRepo;
import com.iddera.usermanagement.api.persistence.repository.redis.UserActivationTokenRepository;
import com.iddera.usermanagement.api.persistence.repository.redis.UserForgotPasswordTokenRepository;
import com.iddera.usermanagement.lib.app.request.*;
import com.iddera.usermanagement.lib.domain.model.EntityStatus;
import com.iddera.usermanagement.lib.domain.model.UserModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.security.Principal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.supplyAsync;


@Service
public class DefaultUserService implements UserService, UserServiceRepo, UserActivationService, UserPasswordService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final Executor executor;
    private final PasswordEncoder encoder;
    private final EmailService emailService;
    private final UserActivationTokenRepository userActivationTokenRepository;
    private final TokenGenerationService tokenGenerationService;
    private final UserForgotPasswordTokenRepository userForgotPasswordTokenRepository;
    private final UserManagementExceptionService exceptions;
    private final MailContentBuilder mailContentBuilder;
    private final EmailConfiguration emailConfiguration;


    public DefaultUserService(UserRepository userRepository,
                              RoleRepository roleRepository,
                              @Qualifier("asyncExecutor") Executor executor,
                              PasswordEncoder encoder,
                              EmailService emailService,
                              UserActivationTokenRepository userActivationTokenRepository,
                              TokenGenerationService tokenGenerationService,
                              UserForgotPasswordTokenRepository userForgotPasswordTokenRepository,
                              UserManagementExceptionService exceptions,
                              MailContentBuilder mailContentBuilder,
                              EmailConfiguration emailConfiguration) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.executor = executor;
        this.encoder = encoder;
        this.emailService = emailService;
        this.userActivationTokenRepository = userActivationTokenRepository;
        this.tokenGenerationService = tokenGenerationService;
        this.userForgotPasswordTokenRepository = userForgotPasswordTokenRepository;
        this.exceptions = exceptions;
        this.mailContentBuilder = mailContentBuilder;
        this.emailConfiguration = emailConfiguration;
    }

    @Override
    public CompletableFuture<UserModel> create(UserRequest request, Locale locale) {
        return createEntity(request, locale).thenApply(User::toModel);
    }

    @Transactional
    @Override
    public CompletableFuture<User> createEntity(UserRequest request, Locale locale) {
        return supplyAsync(() -> {
            ensureUserNameIsUnique(request.getUsername());
            ensureEmailIsUnique(request.getEmail());

            Role role = getRole(request.getRoleId());
            User user = new User()
                    .setFirstName(request.getFirstName())
                    .setLastName(request.getLastName())
                    .setEmail(request.getEmail())
                    .setUsername(request.getUsername())
                    .setPassword(encoder.encode(request.getPassword()))
                    .setDateOfBirth(request.getDateOfBirth())
                    .setRoles(singletonList(role))
                    .setGender(request.getGender())
                    .setType(request.getType())
                    .setStatus(EntityStatus.INACTIVE);

            User savedUser = userRepository.save(user);
            String token = createActivationTokenForUser(user.getUsername());
            String emailHtml = buildNewUserWelcomeMail(user.getUsername(), token, locale);
            emailService.sendEmailToOneAddress(emailHtml,
                    "Welcome to Iddera",
                    user.getEmail(),
                    "notificaiton@iddera.com");
            return savedUser;
        }, executor);
    }

    @Override
    public CompletableFuture<UserModel> update(Long userId, UserUpdateRequest request) {
        return supplyAsync(() -> {
            User user = getUser(userId, () -> exceptions.handleCreateBadRequest("User does not exist"));

            ensureEmailIsUnique(userId, request.getEmail());
            ensureUserNameIsUnique(userId, request.getUsername());
            Role role = getRole(request.getRoleId());

            user.setUsername(request.getUsername())
                    .setEmail(request.getEmail())
                    .setFirstName(request.getFirstName())
                    .setLastName(request.getLastName())
                    .setDateOfBirth(request.getDateOfBirth())
                    .setType(request.getType())
                    .setGender(request.getGender())
                    .setRoles(singletonList(role));

            return userRepository.save(user).toModel();
        }, executor);
    }

    @Override
    public CompletableFuture<Page<UserModel>> getAll(Pageable pageable) {
        return supplyAsync(() ->
                userRepository.findAll(pageable)
                        .map(User::toModel));
    }

    @Override
    public CompletableFuture<UserModel> getById(Long userId) {
        return supplyAsync(() -> {
            User user = getUser(
                    userId,
                    () -> exceptions.handleCreateNotFoundException("User not found", userId));
            return user.toModel();
        });
    }

    @Override
    public CompletableFuture<UserModel> getByUserName(String username) {
        return supplyAsync(() ->
                userRepository.findByUsername(username)
                        .map(User::toModel)
                        .orElseThrow(
                                () -> exceptions.handleCreateNotFoundException("User not found", username)));
    }


    @Override
    public CompletableFuture<UserModel> forgotPassword(String username, Locale locale) {
        return supplyAsync(() ->
                {
                    User user = userRepository.findByUsername(username)
                            .orElseThrow(() -> exceptions.handleCreateNotFoundException("User %s not found", username));
                    String token = createActivationTokenForUser(user.getUsername());
                    String emailHtml = buildForgotPasswordMail(token, locale, username);
                    emailService.sendEmailToOneAddress(emailHtml,
                            "Forgot your password?",
                            user.getEmail(),
                            "notificaiton@iddera.com");
                    return user.toModel();
                }, executor

        );
    }

    @Override
    @Transactional
    public CompletableFuture<UserModel> changePassword(Long userId, ChangeUserPasswordRequest request) {
        return supplyAsync(() -> {
                    User user = getUser(userId, () -> exceptions.handleCreateNotFoundException("User does not exist"));

                    ensureOldPasswordMatches(userId, request.getOldPassword(), user.getPassword());
                    ensureNewPasswordMatchesConfirmedPassword(request.getNewPassword(), request.getConfirmPassword());
                    user.setPassword(encoder.encode(request.getNewPassword()));
                    user = userRepository.save(user);
                    emailService.sendEmailToOneAddress("You have just successfully changed your password",
                            "Password Changed",
                            user.getEmail(),
                            "notificaiton@iddera.com");
                    return user.toModel();
                }
                , executor);
    }

    @Override
    public CompletableFuture<UserModel> verifyUser(UserVerificationRequest userVerificationRequest) {
        User user = userRepository.findByUsername(userVerificationRequest.getUsername())
                .orElseThrow(() -> exceptions.handleCreateNotFoundException("User %s not found", userVerificationRequest.getUsername()));
        UserActivationToken userActivationToken = userActivationTokenRepository
                .findByUsername(
                        userVerificationRequest.getUsername());
        if (userActivationToken == null) {
            createActivationTokenForUser(userVerificationRequest.getUsername());
            throw exceptions.handleCreateBadRequest("This username is not mapped to this activation token");
        }
        if (!userVerificationRequest.getToken().contentEquals(userActivationToken.getActivationToken()))
            throw exceptions.handleCreateBadRequest("This token isn't mapped to the user");
        user.setStatus(EntityStatus.ACTIVE);
        userRepository.save(user);
        return getByUserName(userVerificationRequest.getUsername());
    }

    @Override
    public CompletableFuture<UserModel> getUserDetails(Principal principal) {
        if(principal == null) {
            throw exceptions.handleCreateNotFoundException("Unable to get user details.");
        }

        return supplyAsync(() ->
                userRepository.findByUsername(principal.getName())
                        .map(User::toModel)
                        .orElseThrow(
                                () -> exceptions.handleCreateNotFoundException("User not found", principal.getName())));
    }


    @Override
    public Map<String, Object> getForgotPasswordProperties(String token, String username) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(emailConfiguration.getUserForgotPasswordUrl());
        stringBuilder.append("?tkn=");
        stringBuilder.append(token);
        Map<String, Object> variableMap = new HashMap<>();
        variableMap.put(TemplateConstants.ACTIVATION_KEY, stringBuilder.toString());
        variableMap.put("username", username);
        variableMap.put(TemplateConstants.MINI_TITLE_KEY, TemplateConstants.FORGOT_PASSWORD_MINI_TITLE);
        variableMap.put(TemplateConstants.TITLE_KEY, TemplateConstants.FORGOT_PASSWORD_TITLE);
        variableMap.put(TemplateConstants.MESSAGE_KEY, TemplateConstants.FORGOT_PASSWORD_MESSAGE);
        variableMap.put(TemplateConstants.BUTTON_KEY, TemplateConstants.FORGOT_PASSWORD_BUTTON);
        return variableMap;
    }

    @Override
    public Map<String, Object> getActivateUserProperties(String username, String token) {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(emailConfiguration.getUserActivationUrl());
        stringBuilder.append("?tkn=");
        stringBuilder.append(token);
        Map<String, Object> variableMap = new HashMap<>();
        variableMap.put(TemplateConstants.ACTIVATION_KEY, stringBuilder.toString());
        variableMap.put("username", username);
        variableMap.put(TemplateConstants.MINI_TITLE_KEY, TemplateConstants.ACTIVATE_USER_MINI_TITLE);
        variableMap.put(TemplateConstants.TITLE_KEY, TemplateConstants.ACTIVATE_USER_TITLE);
        variableMap.put(TemplateConstants.MESSAGE_KEY, TemplateConstants.ACTIVATE_USER_MESSAGE);
        variableMap.put(TemplateConstants.BUTTON_KEY, TemplateConstants.ACTIVATE_USER_BUTTON);
        return variableMap;
    }

    @Override
    public CompletableFuture<UserModel> resetPassword(Long id, ForgotPasswordRequest forgotPasswordRequest, Locale locale) {
        return supplyAsync(() -> {
                    User user = getUser(id, () -> exceptions.handleCreateNotFoundException("User does not exist"));

                    ensureNewPasswordMatchesConfirmedPassword(forgotPasswordRequest.getNewPassword(), forgotPasswordRequest.getConfirmPassword());
                    UserForgotPasswordToken userForgotPasswordToken = userForgotPasswordTokenRepository.findByUsername(user.getUsername());
                    if (userForgotPasswordToken == null)
                        throw exceptions.handleCreateNotFoundException("User token not found for user %s", user.getUsername());
                    if (!forgotPasswordRequest.getToken().contentEquals(userForgotPasswordToken.getActivationToken()))
                        throw exceptions.handleCreateBadRequest("This token isn't mapped to the user");
                    user.setPassword(encoder.encode(forgotPasswordRequest.getNewPassword()));
                    user = userRepository.save(user);
                    return user.toModel();
                }

                , executor);
    }

    private Role getRole(Long roleId) {
        return ofNullable(roleId)
                .flatMap(roleRepository::findById)
                .orElseThrow(() -> exceptions.handleCreateBadRequest("Role %d does not exist", roleId));
    }

    private void ensureUserNameIsUnique(String username) {
        ValidationUtil.ensureIsUnique(
                username,
                userRepository::existsByUsername,
                format("Username %s exists", username),
                exceptions);
    }

    private void ensureEmailIsUnique(String email) {
        ValidationUtil.ensureIsUnique(
                email,
                userRepository::existsByEmail,
                format("Email %s exists", email),
                exceptions);
    }

    private User getUser(Long userId, Supplier<RuntimeException> exceptionSupplier) {
        return userRepository.findById(userId)
                .orElseThrow(exceptionSupplier);
    }

    private void ensureOldPasswordMatches(Long userId, String password, String oldPassword) {
        ValidationUtil.ensureIsNotUnique(
                password,
                oldPassword,
                encoder::matches,
                "User password doesn't match old password",
                exceptions
        );
    }

    private void ensureNewPasswordMatchesConfirmedPassword(String newPassword, String confirmedNewPassword) {
        if (!newPassword.contentEquals(confirmedNewPassword))
            throw exceptions.handleCreateBadRequest("Password and confirmed password do not match");
    }

    private void ensureEmailIsUnique(Long userId, String email) {
        ValidationUtil.ensureIsUnique(
                userId,
                email,
                userRepository::existsUserByIdIsNotAndEmail,
                format("Email %s exists", email),
                exceptions);
    }

    private void ensureUserNameIsUnique(Long userId, String username) {
        ValidationUtil.ensureIsUnique(
                userId,
                username,
                userRepository::existsUserByIdIsNotAndUsername,
                format("Username %s exists", username),
                exceptions);
    }

    private String createActivationTokenForUser(String username) {
        UserActivationToken userActivationToken = new UserActivationToken();
        userActivationToken.setUsername(username);
        userActivationToken.setActivationToken(tokenGenerationService.generateToken());
        userActivationToken = userActivationTokenRepository.save(userActivationToken);
        return userActivationToken.getActivationToken();
    }

    private String buildNewUserWelcomeMail(String username, String token, Locale locale) {
        Map<String, Object> variableMap = getActivateUserProperties(username, token);
        return mailContentBuilder.generateMailContent(variableMap, Constants.TEMPLATE, locale);
    }

    private String buildForgotPasswordMail(String token, Locale locale, String username) {
        Map<String, Object> variableMap = getForgotPasswordProperties(token, username);
        return mailContentBuilder.generateMailContent(variableMap, Constants.TEMPLATE, locale);
    }

}
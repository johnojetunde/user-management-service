package com.iddera.usermanagement.api.domain.service.concretes;


import com.iddera.commons.utils.ValidationUtil;
import com.iddera.usermanagement.api.app.config.EmailConfiguration;
import com.iddera.usermanagement.api.app.util.Constants;
import com.iddera.usermanagement.api.domain.exception.UserManagementExceptionService;
import com.iddera.usermanagement.api.domain.service.abstracts.*;
import com.iddera.usermanagement.api.persistence.entity.Role;
import com.iddera.usermanagement.api.persistence.entity.User;
import com.iddera.usermanagement.api.persistence.entity.UserActivationToken;
import com.iddera.usermanagement.api.persistence.entity.UserForgotPasswordToken;
import com.iddera.usermanagement.api.persistence.repository.RoleRepository;
import com.iddera.usermanagement.api.persistence.repository.UserRepository;
import com.iddera.usermanagement.api.persistence.repository.redis.UserActivationTokenRepository;
import com.iddera.usermanagement.api.persistence.repository.redis.UserForgotPasswordTokenRepository;
import com.iddera.usermanagement.lib.app.request.*;
import com.iddera.usermanagement.lib.domain.model.EntityStatus;
import com.iddera.usermanagement.lib.domain.model.UserModel;
import com.iddera.usermanagement.lib.domain.model.UserType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import static com.iddera.usermanagement.api.app.util.TemplateConstants.*;
import static com.iddera.usermanagement.lib.domain.model.EntityStatus.ACTIVE;
import static com.iddera.usermanagement.lib.domain.model.EntityStatus.INACTIVE;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.stream.Collectors.toList;


@Service
public class DefaultUserService implements UserService, UserPasswordService {

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

    @Transactional
    @Override
    public CompletableFuture<UserModel> create(UserRequest request, Locale locale) {
        return createEntity(request, locale).thenApply(User::toModel);
    }

    @Transactional
    @Override
    public CompletableFuture<UserModel> update(Long userId, UserUpdateRequest request) {
        return supplyAsync(() -> {
            User user = getUser(userId, () -> exceptions.handleCreateBadRequest("User does not exist"));

            ensureEmailIsUnique(userId, request.getEmail());
            ensureUserNameIsUnique(userId, request.getUsername());
            Role role = getRole(request.getRoleId(), request.getType());

            user.setUsername(request.getUsername())
                    .setEmail(request.getEmail())
                    .setFirstName(request.getFirstName())
                    .setLastName(request.getLastName())
                    .setType(request.getType())
                    .setRoles(singletonList(role));

            return userRepository.save(user).toModel();
        }, executor);
    }

    @Transactional
    @Override
    public CompletableFuture<UserModel> deactivate(Long userId){
        return supplyAsync(() -> {
            User user = getUser(userId, () -> exceptions.handleCreateBadRequest("User does not exist"));
            if(user.getStatus() == INACTIVE){
                throw exceptions.handleCreateBadRequest("User has already been deactivated.");
            }
            user.setStatus(INACTIVE);
            return userRepository.save(user).toModel();
        }, executor);
    }

    @Override
    public CompletableFuture<Page<UserModel>> getAll(UserType userType, Pageable pageable) {
        return supplyAsync(() ->
                ofNullable(userType)
                        .map(type -> userRepository.findAllByType(type, pageable))
                        .orElseGet(() -> userRepository.findAll(pageable))
                        .map(User::toModel));
    }

    @Override
    public CompletableFuture<UserModel> getById(Long userId) {
        return supplyAsync(() -> {
            User user = getUser(userId, () -> exceptions.handleCreateNotFoundException("User not found", userId));
            return user.toModel();
        });
    }

    @Override
    public CompletableFuture<UserModel> getByUserName(String username) {
        return supplyAsync(() ->
                userRepository.findByUsername(username)
                        .map(User::toModel)
                        .orElseThrow(() -> exceptions.handleCreateNotFoundException("User not found", username)));
    }

    @Override
    public CompletableFuture<UserModel> initiatePasswordReset(EmailModel emailModel, Locale locale) {
        return supplyAsync(() -> {
            User user = getUserByEmail(emailModel.getEmail());

            String token = createForgotTokenForUser(user.getUsername());
            String emailHtml = buildForgotPasswordMail(token, locale, user.getUsername());
            emailService.sendEmailToOneAddress(emailHtml,
                    "Forgot your password?",
                    user.getEmail(),
                    "notificaiton@iddera.com");
            return user.toModel();
        }, executor);
    }

    @Transactional
    @Override
    public CompletableFuture<UserModel> changePassword(Long userId, ChangeUserPasswordRequest request) {
        return supplyAsync(() -> {
            User user = getUser(userId, () -> exceptions.handleCreateBadRequest("User does not exist"));

            ensureOldPasswordMatches(request.getOldPassword(), user.getPassword());
            user.setPassword(encoder.encode(request.getNewPassword()));

            user = userRepository.save(user);
            emailService.sendEmailToOneAddress(
                    "You have just successfully changed your password",
                    "Password Changed",
                    user.getEmail(),
                    "notificaiton@iddera.com");
            return user.toModel();
        }, executor);
    }

    @Override
    public CompletableFuture<UserModel> verifyUser(UserVerificationRequest userVerificationRequest) {
        return supplyAsync(() -> {
            UserActivationToken userActivationToken = userActivationTokenRepository.findByActivationToken(userVerificationRequest.getToken())
                    .orElseThrow(() -> exceptions.handleCreateBadRequest("Invalid token"));

            User user = getUserByEmail(userActivationToken.getUsername());
            user.setStatus(ACTIVE);

            userActivationTokenRepository.deleteById(userActivationToken.getId());
            return userRepository.save(user).toModel();
        }, executor);
    }

    @Override
    public CompletableFuture<UserModel> getUserDetails(Principal principal) {
        return supplyAsync(() ->
                userRepository.findByUsername(principal.getName())
                        .map(User::toModel)
                        .orElseThrow(() -> exceptions.handleCreateNotFoundException("User not found", principal.getName())));
    }

    @Override
    public CompletableFuture<List<UserModel>> getByIds(List<Long> userIds) {
        return supplyAsync(() ->
                userRepository.findAllById(userIds)
                        .stream().map(User::toModel)
                        .collect(toList()));
    }

    @Override
    public CompletableFuture<UserModel> resetPassword(ForgotPasswordRequest request,
                                                      Locale locale) {
        return supplyAsync(() -> {
            UserForgotPasswordToken userForgotPasswordToken = userForgotPasswordTokenRepository.findByActivationToken(request.getToken())
                    .orElseThrow(() -> exceptions.handleCreateBadRequest("Invalid token"));

            User user = getUserByEmail(userForgotPasswordToken.getUsername());
            String hashedPassword = encoder.encode(request.getNewPassword());
            user.setPassword(hashedPassword);

            userForgotPasswordTokenRepository.deleteById(userForgotPasswordToken.getId());
            return userRepository.save(user).toModel();
        }, executor);
    }

    private CompletableFuture<User> createEntity(UserRequest request, Locale locale) {
        return supplyAsync(() -> {
            ensureUserNameIsUnique(request.getUsername());
            ensureEmailIsUnique(request.getEmail());

            Role role = getRole(request.getRoleId(), request.getType());
            boolean isUser = UserType.CLIENT.equals(request.getType());
            User user = new User()
                    .setFirstName(request.getFirstName())
                    .setLastName(request.getLastName())
                    .setEmail(request.getEmail())
                    .setUsername(request.getUsername())
                    .setPassword(encoder.encode(request.getPassword()))
                    .setRoles(List.of(role))
                    .setType(request.getType())
                    .setStatus((isUser) ? INACTIVE : ACTIVE);

            User savedUser = userRepository.save(user);

            String token = createActivationTokenForUser(user.getUsername());
            String emailHtml = buildNewUserWelcomeMail(user.getUsername(), token, locale);
            emailService.sendEmailToOneAddress(emailHtml,
                    "Welcome to Iddera",
                    user.getEmail(),
                    "notification@iddera.com");
            return savedUser;
        }, executor);
    }

    private Role getRole(Long roleId, UserType userType) {
        var roleIdOptional = ofNullable(roleId);

        if (roleIdOptional.isPresent()) {
            return roleIdOptional
                    .flatMap(roleRepository::findById)
                    .orElseThrow(() -> exceptions.handleCreateBadRequest("Role %d does not exist", roleId));
        }

        return createRoleFromUserType(userType);
    }

    private Role createRoleFromUserType(UserType userType) {
        Role role = new Role()
                .setDescription(userType.toString())
                .setName(userType.toString());

        return roleRepository.save(role);
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

    private void ensureOldPasswordMatches(String password, String oldPassword) {
        ValidationUtil.ensureIsNotUnique(
                password,
                oldPassword,
                encoder::matches,
                "User password doesn't match current password",
                exceptions
        );
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
        var token = tokenGenerationService.generateToken();
        UserActivationToken userActivationToken = new UserActivationToken();
        userActivationToken.setUsername(username);
        userActivationToken.setActivationToken(token);

        userActivationTokenRepository.save(userActivationToken);
        return token;
    }

    private String createForgotTokenForUser(String username) {
        var token = tokenGenerationService.generateToken();
        UserForgotPasswordToken forgotPasswordToken = new UserForgotPasswordToken();
        forgotPasswordToken.setUsername(username);
        forgotPasswordToken.setActivationToken(token);

        userForgotPasswordTokenRepository.save(forgotPasswordToken);
        return token;
    }

    private Map<String, Object> getForgotPasswordProperties(String token, String username) {
        Map<String, Object> variableMap = new HashMap<>();
        String stringBuilder = emailConfiguration.getUserForgotPasswordUrl() + "?tkn=" + token;
        variableMap.put(ACTIVATION_KEY, stringBuilder);
        variableMap.put("username", username);
        variableMap.put(MINI_TITLE_KEY, FORGOT_PASSWORD_MINI_TITLE);
        variableMap.put(TITLE_KEY, FORGOT_PASSWORD_TITLE);
        variableMap.put(MESSAGE_KEY, FORGOT_PASSWORD_MESSAGE);
        variableMap.put(BUTTON_KEY, FORGOT_PASSWORD_BUTTON);
        return variableMap;
    }

    private Map<String, Object> getActivateUserProperties(String username, String token) {
        Map<String, Object> variableMap = new HashMap<>();
        String stringBuilder = emailConfiguration.getUserActivationUrl() + "?tkn=" + token;
        variableMap.put(ACTIVATION_KEY, stringBuilder);
        variableMap.put("username", username);
        variableMap.put(MINI_TITLE_KEY, ACTIVATE_USER_MINI_TITLE);
        variableMap.put(TITLE_KEY, ACTIVATE_USER_TITLE);
        variableMap.put(MESSAGE_KEY, ACTIVATE_USER_MESSAGE);
        variableMap.put(BUTTON_KEY, ACTIVATE_USER_BUTTON);
        return variableMap;
    }

    private String buildNewUserWelcomeMail(String username, String token, Locale locale) {
        Map<String, Object> variableMap = getActivateUserProperties(username, token);
        return mailContentBuilder.generateMailContent(variableMap, Constants.WELCOME_TEMPLATE, locale);
    }

    private String buildForgotPasswordMail(String token, Locale locale, String username) {
        Map<String, Object> variableMap = getForgotPasswordProperties(token, username);
        return mailContentBuilder.generateMailContent(variableMap, Constants.FORGOT_PASSWORD_TEMPLATE, locale);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> exceptions.handleCreateBadRequest("User %s not found", email));
    }
}
package com.iddera.usermanagement.api.domain.service.concretes;


import com.iddera.commons.utils.ValidationUtil;
import com.iddera.usermanagement.api.domain.exception.UserManagementExceptionService;
import com.iddera.usermanagement.api.domain.service.abstracts.EmailService;
import com.iddera.usermanagement.api.domain.service.abstracts.TokenGenerationService;
import com.iddera.usermanagement.api.domain.service.abstracts.UserService;
import com.iddera.usermanagement.api.persistence.entity.Role;
import com.iddera.usermanagement.api.persistence.entity.User;
import com.iddera.usermanagement.api.persistence.entity.UserActivationToken;
import com.iddera.usermanagement.api.persistence.repository.RoleRepository;
import com.iddera.usermanagement.api.persistence.repository.UserRepository;
import com.iddera.usermanagement.api.persistence.repository.UserServiceRepo;
import com.iddera.usermanagement.api.persistence.repository.redis.UserActivationTokenRepository;
import com.iddera.usermanagement.lib.app.request.ChangeUserPasswordRequest;
import com.iddera.usermanagement.lib.app.request.UserRequest;
import com.iddera.usermanagement.lib.app.request.UserUpdateRequest;
import com.iddera.usermanagement.lib.app.request.UserVerificationRequest;
import com.iddera.usermanagement.lib.domain.model.EntityStatus;
import com.iddera.usermanagement.lib.domain.model.UserModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.supplyAsync;


@Service
public class DefaultUserService implements UserService, UserServiceRepo {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final Executor executor;
    private final PasswordEncoder encoder;
    private final EmailService emailService;
    private final UserActivationTokenRepository userActivationTokenRepository;
    private final TokenGenerationService tokenGenerationService;
    private final String activationUrl;
    private final UserManagementExceptionService exceptions;

    public DefaultUserService(UserRepository userRepository,
                              RoleRepository roleRepository,
                              @Qualifier("asyncExecutor") Executor executor,
                              PasswordEncoder encoder,
                              EmailService emailService,
                              UserActivationTokenRepository userActivationTokenRepository,
                              TokenGenerationService tokenGenerationService,
                              @Value("${notification-client-url: http://localhost:8080/users/verify-email}") String activationUrl,
                              UserManagementExceptionService exceptions) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.executor = executor;
        this.encoder = encoder;
        this.emailService = emailService;
        this.userActivationTokenRepository = userActivationTokenRepository;
        this.tokenGenerationService = tokenGenerationService;
        this.activationUrl = activationUrl;
        this.exceptions = exceptions;
    }

    @Override
    public CompletableFuture<UserModel> create(UserRequest request) {
        return createEntity(request).thenApply(User::toModel);
    }

    @Transactional
    @Override
    public CompletableFuture<User> createEntity(UserRequest request) {
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
            emailService.sendEmailToOneAddress(String.format("You've successfully created your user with username: %s %n Click on the link to activate your user %s?tkn=%s",
                    user.getUsername(), activationUrl, token),
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
}
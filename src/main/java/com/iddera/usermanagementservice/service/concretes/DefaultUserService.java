package com.iddera.usermanagementservice.service.concretes;


import com.iddera.usermanagementservice.entity.Role;
import com.iddera.usermanagementservice.entity.User;
import com.iddera.usermanagementservice.entity.redis.UserActivationToken;
import com.iddera.usermanagementservice.model.EntityStatus;
import com.iddera.usermanagementservice.model.UserModel;
import com.iddera.usermanagementservice.repository.RoleRepository;
import com.iddera.usermanagementservice.repository.UserRepository;
import com.iddera.usermanagementservice.repository.UserServiceRepo;
import com.iddera.usermanagementservice.repository.redis.UserActivationTokenRepository;
import com.iddera.usermanagementservice.request.ChangeUserPasswordRequest;
import com.iddera.usermanagementservice.request.UserRequest;
import com.iddera.usermanagementservice.request.UserUpdateRequest;
import com.iddera.usermanagementservice.request.UserVerificationRequest;
import com.iddera.usermanagementservice.service.abstracts.EmailService;
import com.iddera.usermanagementservice.service.abstracts.TokenGenerationService;
import com.iddera.usermanagementservice.service.abstracts.UserService;
import com.iddera.usermanagementservice.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import static com.iddera.usermanagementservice.util.ExceptionUtil.handleCreateBadRequest;
import static com.iddera.usermanagementservice.util.ExceptionUtil.handleCreateNotFoundException;
import static com.iddera.usermanagementservice.util.FunctionUtil.emptyIfNullStream;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.stream.Collectors.toList;


@Service
public class DefaultUserService implements UserService, UserServiceRepo {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final Executor executor;
    private final PasswordEncoder encoder;
    private final EmailService emailService;
    private final UserActivationTokenRepository userActivationTokenRepository;
    private final TokenGenerationService tokenGenerationService;
    @Value("${notification-client-url: localhost:8080/users/verify-email}")
    private String activationUrl;

    public DefaultUserService(UserRepository userRepository,
                              RoleRepository roleRepository,
                              @Qualifier("asyncExecutor") Executor executor,
                              PasswordEncoder encoder,
                              EmailService emailService,
                              UserActivationTokenRepository userActivationTokenRepository,
                              TokenGenerationService tokenGenerationService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.executor = executor;
        this.encoder = encoder;
        this.emailService = emailService;
        this.userActivationTokenRepository = userActivationTokenRepository;
        this.tokenGenerationService = tokenGenerationService;
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
            User user = getUser(userId, () -> handleCreateBadRequest("User does not exist"));

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
                    () -> handleCreateNotFoundException("User not found", userId));
            return user.toModel();
        });
    }

    @Override
    public CompletableFuture<UserModel> getByUserName(String username) {
        return supplyAsync(() ->
                userRepository.findByUsername(username)
                        .map(User::toModel)
                        .orElseThrow(
                                () -> handleCreateNotFoundException("User not found", username)));
    }

    @Override
    @Transactional
    public CompletableFuture<UserModel> changePassword(Long userId, ChangeUserPasswordRequest request) {
        return supplyAsync(() -> {
                    User user = getUser(userId, () -> handleCreateNotFoundException("User does not exist"));

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
        User user = userRepository.findByUsername(userVerificationRequest.getUsername()).orElseThrow(() -> handleCreateNotFoundException("User %s not found", userVerificationRequest.getUsername()));
        UserActivationToken userActivationToken = userActivationTokenRepository
                .findByUsername(
                        userVerificationRequest.getUsername());
        if (userActivationToken == null) {
            createActivationTokenForUser(userVerificationRequest.getUsername());
            throw handleCreateBadRequest("This username is not mapped to this activation token");
        }
        if (!userVerificationRequest.getToken().contentEquals(userActivationToken.getActivationToken()))
            throw handleCreateBadRequest("This token isn't mapped to the user");
        user.setStatus(EntityStatus.ACTIVE);
        userRepository.save(user);
        return getByUserName(userVerificationRequest.getUsername());
    }

    private Role getRole(Long roleId) {
        return ofNullable(roleId)
                .flatMap(roleRepository::findById)
                .orElseThrow(() -> handleCreateBadRequest("Role %d does not exist", roleId));
    }

    private void ensureUserNameIsUnique(String username) {
        ValidationUtil.ensureIsUnique(
                username,
                userRepository::existsByUsername,
                format("Username %s exists", username));
    }

    private void ensureEmailIsUnique(String email) {
        ValidationUtil.ensureIsUnique(
                email,
                userRepository::existsByEmail,
                format("Email %s exists", email));
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
                "User password doesn't match old password"
        );
    }

    private void ensureNewPasswordMatchesConfirmedPassword(String newPassword, String confirmedNewPassword) {
        if (!newPassword.contentEquals(confirmedNewPassword))
            throw handleCreateBadRequest("Password and confirmed password do not match");
    }

    private void ensureEmailIsUnique(Long userId, String email) {
        ValidationUtil.ensureIsUnique(
                userId,
                email,
                userRepository::existsUserByIdIsNotAndEmail,
                format("Email %s exists", email));
    }

    private void ensureUserNameIsUnique(Long userId, String username) {
        ValidationUtil.ensureIsUnique(
                userId,
                username,
                userRepository::existsUserByIdIsNotAndUsername,
                format("Username %s exists", username));
    }

    private String createActivationTokenForUser(String username) {
        UserActivationToken userActivationToken = new UserActivationToken();
        userActivationToken.setUsername(username);
        userActivationToken.setActivationToken(tokenGenerationService.generateToken());
        userActivationToken = userActivationTokenRepository.save(userActivationToken);
        return userActivationToken.getActivationToken();
    }
}
package com.iddera.usermanagement.api.persistence.repository;

import com.iddera.usermanagement.api.persistence.entity.User;
import com.iddera.usermanagement.lib.domain.model.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsUserByIdIsNotAndUsername(Long userId, String username);

    boolean existsUserByIdIsNotAndEmail(Long userId, String email);

    Page<User> findAllByType(UserType userType, Pageable pageable);
}
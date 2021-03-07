package com.iddera.usermanagement.lib.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Accessors(chain = true)
@NoArgsConstructor
@Data
public class UserModel {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
    private UserType type;
    private Gender gender;
    private LocalDateTime lastLoginDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Set<RoleModel> role;
}
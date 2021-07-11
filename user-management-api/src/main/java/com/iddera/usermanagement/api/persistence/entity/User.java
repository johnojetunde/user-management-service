package com.iddera.usermanagement.api.persistence.entity;


import com.iddera.usermanagement.lib.domain.model.EntityStatus;
import com.iddera.usermanagement.lib.domain.model.RoleModel;
import com.iddera.usermanagement.lib.domain.model.UserModel;
import com.iddera.usermanagement.lib.domain.model.UserType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;

import static com.iddera.commons.utils.FunctionUtil.emptyIfNullStream;
import static java.util.stream.Collectors.toSet;
import static javax.persistence.FetchType.EAGER;


@Accessors(chain = true)
@Data
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user", schema = "public")
public class User extends BaseEntity {
    private String firstName;
    private String lastName;
    @Column(unique = true)
    private String email;
    @Column(unique = true)
    private String username;
    private String password;
    @Enumerated(EnumType.STRING)
    private UserType type;
    private LocalDateTime lastLoginDate;
    @Enumerated(EnumType.STRING)
    private EntityStatus status;
    @ManyToMany(fetch = EAGER)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(
                    name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(
                    name = "role_id", referencedColumnName = "id"
            )
    )
    private Collection<Role> roles;

    public UserModel toModel() {
        return new UserModel()
                .setId(getId())
                .setFirstName(getFirstName())
                .setLastName(getLastName())
                .setEmail(getEmail())
                .setUsername(getUsername())
                .setType(getType())
                .setLastLoginDate(getLastLoginDate())
                .setCreatedAt(getCreatedAt())
                .setUpdatedAt(getUpdatedAt())
                .setCreatedBy(getCreatedBy())
                .setUpdatedBy(getUpdatedBy())
                .setRole(getRoleModels());
    }

    private Set<RoleModel> getRoleModels() {
        return emptyIfNullStream(getRoles())
                .map(Role::toModel)
                .collect(toSet());
    }
}
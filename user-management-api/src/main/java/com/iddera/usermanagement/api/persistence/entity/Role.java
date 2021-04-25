package com.iddera.usermanagement.api.persistence.entity;


import com.iddera.usermanagement.lib.domain.model.RoleModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;

@Accessors(chain = true)
@Data
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "role", schema = "public")
public class Role extends BaseEntity {
    private String name;
    private String description;

    public RoleModel toModel() {
        return new RoleModel().setDescription(description).setName(name).setId(id);
    }
}

package com.iddera.usermanagement.lib.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


@Accessors(chain = true)
@NoArgsConstructor
@Data
public class RoleModel {
    private Long id;
    private String name;
    private String description;
}

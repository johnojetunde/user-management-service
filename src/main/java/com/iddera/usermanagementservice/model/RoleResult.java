package com.iddera.usermanagementservice.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

public class RoleResult extends PageImpl<RoleModel> {
    public RoleResult(Page<RoleModel> pagedContent) {
        super(pagedContent.getContent(), pagedContent.getPageable(), pagedContent.getTotalElements());
    }
}

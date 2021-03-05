package com.iddera.usermanagementservice.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

public class UserResult extends PageImpl<UserModel> {
    public UserResult(Page<UserModel> pagedContent) {
        super(pagedContent.getContent(), pagedContent.getPageable(), pagedContent.getTotalElements());
    }
}

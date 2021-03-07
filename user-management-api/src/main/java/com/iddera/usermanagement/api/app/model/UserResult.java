package com.iddera.usermanagement.api.app.model;

import com.iddera.usermanagement.lib.domain.model.UserModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

public class UserResult extends PageImpl<UserModel> {
    public UserResult(Page<UserModel> pagedContent) {
        super(pagedContent.getContent(), pagedContent.getPageable(), pagedContent.getTotalElements());
    }
}

package com.iddera.usermanagement.api.domain.service.concretes;

import com.iddera.usermanagement.api.persistence.entity.User;
import com.iddera.usermanagement.lib.app.request.PinUpdate;

public class TestDataFixtures {
    public static User mockUser() {
        return new User()
                .setEmail("email@email.com")
                .setFirstName("firstname")
                .setLastName("lastname");
    }

    public static PinUpdate pinUpdate() {
        return new PinUpdate("password", "1345", "1234", "1234");
    }
}

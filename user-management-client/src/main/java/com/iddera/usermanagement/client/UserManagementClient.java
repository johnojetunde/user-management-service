package com.iddera.usermanagement.client;

import com.iddera.client.provider.RetrofitProvider;
import com.iddera.usermanagement.client.endpoints.Users;
import com.iddera.usermanagement.client.retrofits.UserClient;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserManagementClient {
    private final RetrofitProvider retrofitProvider;
    private final String baseUrl;

    public Users users() {
        return new Users(retrofitProvider.initializer(UserClient.class, baseUrl));
    }
}

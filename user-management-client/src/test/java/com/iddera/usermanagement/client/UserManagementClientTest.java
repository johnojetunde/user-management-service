package com.iddera.usermanagement.client;

import com.iddera.client.model.ResponseModel;
import com.iddera.client.provider.RetrofitProvider;
import com.iddera.usermanagement.lib.app.request.UserRequest;
import com.iddera.usermanagement.lib.domain.model.Gender;
import com.iddera.usermanagement.lib.domain.model.UserModel;
import com.iddera.usermanagement.lib.domain.model.UserType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
class UserManagementClientTest {
    /***
     * To use the user-management-lient-lib, you need to do the following:
     * 1. add notification-client-lib as a dependency in your project
     *            <dependency>
     *             <groupId>com.iddera</groupId>
     *             <artifactId>user-management-client-lib</artifactId>
     *             <version>1.0</version>
     *         </dependency>
     *
     * 2. Create a bean for the Retrofit provider with the timout in seconds
     * 3. Create another bean for the UserManagementClient just as written down here
     * 4. Use the endpoint/controller you want to use
     * */

    @Disabled
    @Test
    void createUSerWithBadData() {
        RetrofitProvider retrofitProvider = new RetrofitProvider(300, 30, 30);
        UserManagementClient userManagementClient = new UserManagementClient(retrofitProvider, "https://iddera-notification-api.herokuapp.com/");
        var request = new UserRequest();

        ResponseModel<UserModel> response = userManagementClient.users().create(request).join();

        assertFalse(response.isSuccessful());
        assertEquals(400, response.getStatus());
        assertFalse(response.getErrors().isEmpty());
    }

    @Disabled
    @Test
    void createUser() {
        RetrofitProvider retrofitProvider = new RetrofitProvider(300, 30, 30);
        UserManagementClient userManagementClient = new UserManagementClient(retrofitProvider, "https://iddera-notification-api.herokuapp.com/");
        var request = new UserRequest()
                .setEmail("hello@hello.com")
                .setDateOfBirth(LocalDate.now())
                .setGender(Gender.MALE)
                .setFirstName("Iddera")
                .setLastName("Health")
                .setUsername("hello@iddera.com")
                .setPassword("password")
                .setConfirmPassword("password")
                .setType(UserType.CLIENT)
                .setRoleId(1L);

        ResponseModel<UserModel> response = userManagementClient.users().create(request).join();

        assertTrue(response.isSuccessful());
        assertNotNull(response.getData());
        assertNull(response.getErrors());
    }
}
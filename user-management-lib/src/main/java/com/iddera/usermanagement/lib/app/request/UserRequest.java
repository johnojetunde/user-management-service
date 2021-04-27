package com.iddera.usermanagement.lib.app.request;

import com.iddera.commons.annotation.FieldMatch;
import com.iddera.usermanagement.lib.domain.model.UserType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@NoArgsConstructor
@Accessors(chain = true)
@Data
@FieldMatch(
        first = "password",
        second = "confirmPassword",
        message = "Password and confirm password does not match")
public class UserRequest {
    @NotBlank(message = "Firstname is required")
    private String firstName;
    @NotBlank(message = "Lastname is required")
    private String lastName;
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email supplied")
    private String email;
    @NotBlank(message = "Username is required")
    private String username;
    @NotNull
    private UserType type;
    @NotBlank
    @Size(min = 6)
    private String password;
    @NotBlank
    @Size(min = 6)
    private String confirmPassword;
    private Long roleId;

}
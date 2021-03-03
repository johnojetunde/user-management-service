package com.iddera.usermanagementservice.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iddera.usermanagementservice.model.Gender;
import com.iddera.usermanagementservice.model.UserType;
import com.iddera.usermanagementservice.validator.annotation.FieldMatch;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

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
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date dateOfBirth;
    @NotNull
    private UserType type;
    @NotNull
    private Gender gender;
    @NotBlank
    @Size(min = 6)
    private String password;
    @NotBlank
    @Size(min = 6)
    private String confirmPassword;
    @NotNull
    private Long roleId;

}
package com.iddera.usermanagementservice.request;

import com.iddera.usermanagementservice.model.Gender;
import com.iddera.usermanagementservice.model.UserType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Date;

@NoArgsConstructor
@Accessors(chain = true)
@Data
public class UserUpdateRequest {
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
    private Date dateOfBirth;
    @NotNull
    private UserType type;
    @NotNull
    private Long roleId;
    @NotNull
    private Gender gender;
}
package com.iddera.usermanagement.lib.app.request;

import com.iddera.commons.annotation.ValidEnum;
import com.iddera.usermanagement.lib.domain.model.Gender;
import com.iddera.usermanagement.lib.domain.model.UserType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

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
    private LocalDateTime dateOfBirth;
    @NotNull
    @ValidEnum(message = "Invalid UserType")
    private UserType type;
    @NotNull
    private Long roleId;
    @NotNull
    @ValidEnum(message = "Invalid value for gender")
    private Gender gender;
}
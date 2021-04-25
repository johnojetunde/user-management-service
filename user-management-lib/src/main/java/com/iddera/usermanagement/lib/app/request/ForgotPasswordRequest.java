package com.iddera.usermanagement.lib.app.request;

import com.iddera.commons.annotation.FieldMatch;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@Accessors(chain = true)
@Data
@FieldMatch(
        first = "newPassword",
        second = "confirmPassword",
        message = "Password and confirm password does not match")
public class ForgotPasswordRequest {
    @NotBlank(message = "Token is required")
    String token;

    @NotBlank(message = "New password is required")
    String newPassword;

    @NotBlank(message = "Confirmed new password is required")
    String confirmPassword;
}

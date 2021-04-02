package com.iddera.usermanagement.lib.app.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@Accessors(chain = true)
@Data
public class ForgotPasswordRequest {
    @NotBlank(message = "Token is required")
    String token;

    @NotBlank(message = "New password is required")
    String newPassword;

    @NotBlank(message = "Confirmed new password is required")
    String confirmPassword;
}

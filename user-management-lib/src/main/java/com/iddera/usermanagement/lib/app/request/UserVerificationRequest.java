package com.iddera.usermanagement.lib.app.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@Accessors(chain = true)
@Data
public class UserVerificationRequest {
    @NotBlank(message = "Token is required")
    private String token;
    @NotBlank(message = "Username is required")
    private String username;
}

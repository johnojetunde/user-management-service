package com.iddera.usermanagement.lib.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class LoginModel {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}

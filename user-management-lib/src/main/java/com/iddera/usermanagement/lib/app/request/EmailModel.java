package com.iddera.usermanagement.lib.app.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class EmailModel {
    @Email(message = "Email is invalid")
    @NotBlank(message = "Email is required")
    private String email;
}

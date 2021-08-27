package com.iddera.usermanagement.lib.app.request;


import com.iddera.commons.annotation.FieldMatch;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Data
@FieldMatch(
        first = "newPin",
        second = "confirmNewPin",
        message = "NewPIN and confirmNewPin does not match")
public class PinUpdate {
    private String password;
    private String currentPin;
    @NotBlank
    @Size(min = 4, max = 4)
    private String newPin;
    @NotBlank
    @Size(min = 4, max = 4)
    private String confirmNewPin;
}

package com.iddera.usermanagement.lib.app.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class EmailValidationResponse {
    private boolean isExisting;
    private String email;
}

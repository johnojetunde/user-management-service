package com.iddera.usermanagement.lib.app.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@NoArgsConstructor
@Accessors(chain = true)
@Data
public class UserSearch {
    @NotEmpty
    private List<Long> ids;
}

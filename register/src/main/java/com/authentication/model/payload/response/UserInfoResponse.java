package com.authentication.model.payload.response;
import java.util.List;

import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(chain = true)
public class UserInfoResponse {
    private Long id;
    private String username;
    private String email;
    private List<String> roles;
}

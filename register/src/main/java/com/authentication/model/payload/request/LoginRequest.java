package com.authentication.model.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(chain = true)
public class LoginRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}


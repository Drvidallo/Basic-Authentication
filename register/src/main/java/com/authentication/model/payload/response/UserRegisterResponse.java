package com.authentication.model.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper=false)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterResponse extends BaseResponse {
    private Long id;
    private String username;
}

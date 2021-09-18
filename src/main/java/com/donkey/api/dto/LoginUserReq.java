package com.donkey.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginUserReq {
    private String email;
    private String password;
}

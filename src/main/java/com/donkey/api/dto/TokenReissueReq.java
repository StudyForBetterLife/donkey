package com.donkey.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenReissueReq {

    private String email;
    private String accessToken;
    private String refreshToken;
}

package com.donkey.api.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TokenReissueRes {
    private boolean success;
    private String message;
    private String accessToken;

    @Builder
    public TokenReissueRes(boolean success, String message, String accessToken) {
        this.success = success;
        this.message = message;
        this.accessToken = accessToken;
    }
}

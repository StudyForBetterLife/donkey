package com.donkey.api.dto;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginRes {

    private boolean success;

    private String message;

    @ApiModelProperty(example = "쿠키값으로 전달")
    private String accessToken;

    @ApiModelProperty(example = "쿠키값으로 전달")
    private String refreshToken;

    @Builder
    public LoginRes(boolean success, String message, String accessToken, String refreshToken) {
        this.success = success;
        this.message = message;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}

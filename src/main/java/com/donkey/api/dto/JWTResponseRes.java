package com.donkey.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JWTResponseRes {

    private boolean success;
    private boolean expired;
    private String message;

    @Builder
    public JWTResponseRes(boolean success, boolean expired, String message) {
        this.success = success;
        this.expired = expired;
        this.message = message;
    }
}

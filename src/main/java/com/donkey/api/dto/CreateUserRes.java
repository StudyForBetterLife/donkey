package com.donkey.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateUserRes{

    private boolean success;
    private String message;
    @ApiModelProperty(example = "유저 pk 값")
    private Long id;

    @Builder
    public CreateUserRes(boolean success, String message, Long id) {
        this.success = success;
        this.message = message;
        this.id = id;
    }
}

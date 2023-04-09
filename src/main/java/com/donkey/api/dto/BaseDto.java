package com.donkey.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class BaseDto {

    private boolean success;
    private String message;

    public BaseDto() {

    }

}

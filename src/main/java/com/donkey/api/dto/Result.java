package com.donkey.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Result<T> {

    @ApiModelProperty(example = "요청 성공 여부 (true/false)")
    private boolean success;

    @ApiModelProperty(example = "추가 메시지")
    private String message;

    @ApiModelProperty(example = "응답 데이터")
    private T data;

    @Builder
    public Result(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
}

package com.donkey.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserReq {

    @ApiModelProperty(example = "NotNull")
    private String email;

    @ApiModelProperty(example = "NotNull")
    private String name;

    @ApiModelProperty(example = "NotNull")
    private String userId;

    private String nickName;

    @ApiModelProperty(example = "NotNull")
    private String password;

    @ApiModelProperty(example = "NotNull")
    private String telNum;
}

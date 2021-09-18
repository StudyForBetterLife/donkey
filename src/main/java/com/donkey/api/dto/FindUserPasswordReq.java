package com.donkey.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FindUserPasswordReq {

    private String email;
    private String userId;
    private String telNum;
}

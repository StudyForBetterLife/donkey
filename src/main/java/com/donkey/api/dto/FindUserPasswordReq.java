package com.donkey.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FindUserPasswordReq {

    private String email;
    private String userId;
    private String telNum;
}

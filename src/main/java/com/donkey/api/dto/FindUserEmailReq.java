package com.donkey.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FindUserEmailReq {

    private String userId;
    private String telNum;
}

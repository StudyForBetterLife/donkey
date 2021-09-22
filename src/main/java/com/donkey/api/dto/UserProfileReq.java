package com.donkey.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserProfileReq {
    private String email;

    /**
     * 필드가 1개인 경우 AllArgsConstructor 어노테이션을 사용하면 에러발생
     */
    public UserProfileReq(String email) {
        this.email = email;
    }
}

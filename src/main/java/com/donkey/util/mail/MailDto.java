package com.donkey.util.mail;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MailDto {
    private String address;
    private String userName;
    private String token;

    @Builder
    public MailDto(String address, String userName, String token) {
        this.address = address;
        this.userName = userName;
        this.token = token;
    }
}

package com.donkey.util.mail;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MailDto {
    private String to;
    private String subject;
    private String userName;
    private String token;

    @Builder
    public MailDto(String to, String subject, String userName, String token) {
        this.to = to;
        this.subject = subject;
        this.userName = userName;
        this.token = token;
    }
}

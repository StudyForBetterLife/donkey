package com.donkey.domain.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserType {
    UNCERTIFIED("UNCERTIFIED","UNCERTIFIED"),
    CERTIFIED("CERTIFIED","CERTIFIED");

    private final String key;
    private final String value;
}

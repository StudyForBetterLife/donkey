package com.donkey.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserType {
    NORMAL("NORMAL","일반 사용자"),
    DELETE("CERTIFIED","인증된사용자");

    private final String key;
    private final String value;
}

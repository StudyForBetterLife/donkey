package com.donkey.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthProvider {
    LOCAL("LOCAL", "LOCAL"),
    APPLE("APPLE", "APPLE"),
    NAVER("NAVER", "NAVER"),
    GOOGLE("GOOGLE", "GOOGLE"),
    KAKAO("KAKAO", "KAKAO");

    private final String key;
    private final String value;
}

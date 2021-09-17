package com.donkey.domain.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public enum AuthProvider {
    local,
    apple,
    kakao,
    google,
    naver
}


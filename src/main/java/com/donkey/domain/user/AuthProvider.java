package com.donkey.domain.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public enum AuthProvider {
    none,
    donkey,
    apple,
    kakao,
    google,
    naver
}


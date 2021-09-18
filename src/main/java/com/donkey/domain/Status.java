package com.donkey.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Status {
    NORMAL("NORMAL","일반"),
    DELETE("DELETE","삭제");

    private final String key;
    private final String value;
}

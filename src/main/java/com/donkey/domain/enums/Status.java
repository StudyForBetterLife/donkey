package com.donkey.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Status {
    NORMAL("NORMAL","�Ϲ�"),
    DELETE("DELETE","����");

    private final String key;
    private final String value;
}

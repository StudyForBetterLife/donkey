package com.donkey.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserType {
    NORMAL("NORMAL","�Ϲ� �����"),
    DELETE("CERTIFIED","�����Ȼ����");

    private final String key;
    private final String value;
}

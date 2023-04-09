package com.donkey.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
public class BaseRes extends BaseDto{

    @Builder
    public BaseRes(boolean success, String message) {
        super(success, message);
    }

}

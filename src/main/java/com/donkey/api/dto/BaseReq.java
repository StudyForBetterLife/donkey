package com.donkey.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
public class BaseReq extends BaseDto{

    @Builder
    public BaseReq(boolean success, String message) {
        super(success, message);
    }

}

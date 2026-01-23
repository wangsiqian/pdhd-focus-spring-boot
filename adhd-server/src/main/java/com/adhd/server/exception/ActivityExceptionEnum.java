package com.adhd.server.exception;

import com.adhd.server.common.exception.ApiExceptionEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author adhd
 */
@RequiredArgsConstructor
@Getter
public enum ActivityExceptionEnum implements ApiExceptionEnum {
    ACTIVITY_NOT_FOUND(32000, "实际事项未找到");

    private final Integer code;
    private final String message;
}
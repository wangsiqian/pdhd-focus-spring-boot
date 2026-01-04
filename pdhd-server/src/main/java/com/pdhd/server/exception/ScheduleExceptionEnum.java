package com.pdhd.server.exception;

import com.pdhd.server.common.exception.ApiExceptionEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author pdhd
 */
@RequiredArgsConstructor
@Getter
public enum ScheduleExceptionEnum implements ApiExceptionEnum {
    SCHEDULE_NOT_FOUND(31000, "计划未找到");

    private final Integer code;
    private final String message;
}
package com.adhd.server.exception;

import com.adhd.server.common.exception.ApiExceptionEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author adhd
 */
@RequiredArgsConstructor
@Getter
public enum ScheduleExceptionEnum implements ApiExceptionEnum {
    SCHEDULE_NOT_FOUND(31000, "计划未找到"),
    SCHEDULE_LOCK_BUSY(31001, "计划处理中，请稍后再试"),
    REPEAT_RULE_CONFIG_INVALID(31002, "重复规则配置不合法");

    private final Integer code;
    private final String message;
}

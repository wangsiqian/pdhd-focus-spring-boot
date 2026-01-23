package com.adhd.server.exception;

import com.adhd.server.common.exception.ApiExceptionEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author adhd
 */
@RequiredArgsConstructor
@Getter
public enum GoalExceptionEnum implements ApiExceptionEnum {
    GOAL_NOT_FOUND(30000, "目标未找到");

    private final Integer code;
    private final String message;
}
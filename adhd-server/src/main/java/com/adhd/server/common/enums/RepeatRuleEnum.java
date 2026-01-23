package com.adhd.server.common.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author adhd
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum RepeatRuleEnum {
    NONE("不重复"),
    DAILY("每天"),
    WEEKDAY("工作日"),
    CUSTOM("自定义");

    private final String desc;
}

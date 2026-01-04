package com.pdhd.server.common.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author pdhd
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum RepeatRuleEnum {
    NONE("不重复"),
    CUSTOM("自定义");

    private final String desc;
}
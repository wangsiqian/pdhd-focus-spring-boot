package com.pdhd.api.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author wangsiqian
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum UserTypeEnum {
    GUEST("访客"),
    REGISTERED("注册用户");

    private final String desc;
}

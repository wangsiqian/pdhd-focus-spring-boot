package com.pdhd.server.exception;

import com.pdhd.server.common.exception.ApiExceptionEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author wangsiqian
 */
@RequiredArgsConstructor
@Getter
public enum UserExceptionEnum implements ApiExceptionEnum {
    USER_NOT_FOUND(10000, "用户未找到"),
    ACCOUNT_ALREADY_EXIST(10001, "用户已经存在了"),
    ACCOUNT_OR_PASSWORD_WRONG(10002, "账号或密码错误");

    private final Integer code;
    private final String message;
}

package com.adhd.server.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 返回结果统一状态码枚举类
 *
 * @author wangsiqian
 */
@Getter
@AllArgsConstructor
public enum ResponseCodeEnum {
    /**
     * 成功返回的状态码
     */
    SUCCESS(0, "成功"),
    /**
     * 请求错误的异常状态码
     */
    BAD_REQUEST(400, "请求参数异常"),
    /**
     * 身份验证失败
     */
    UNAUTHORIZED(401, "您没有访问该接口的权限"),
    /**
     * 默认服务器异常状态码
     */
    INTERNAL_SERVER_ERROR(500, "服务异常");

    private final Integer code;

    private final String message;
}

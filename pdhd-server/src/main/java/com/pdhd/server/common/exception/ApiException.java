package com.pdhd.server.common.exception;

import com.pdhd.server.common.ResponseCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 自定义的API异常
 *
 * @author wangsiqian
 */
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@Data
public class ApiException extends RuntimeException {
    private Integer code;
    private String message;

    public ApiException() {
        this("A server error occurred.");
    }

    public ApiException(String message) {
        this(ResponseCodeEnum.SUCCESS.getCode(), message);
    }

    public ApiException(ApiExceptionEnum exception) {
        this(exception.getCode(), exception.getMessage());
    }
}

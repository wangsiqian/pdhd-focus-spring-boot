package com.adhd.server.common.exception;

/**
 * @author wangsiqian
 */
public interface ApiExceptionEnum {
    /**
     * 异常码
     */
    Integer getCode();

    /**
     * 获取异常信息
     *
     * @return 异常信息
     * @author wangsiqian
     */
    String getMessage();
}

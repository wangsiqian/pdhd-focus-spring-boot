package com.pdhd.server.common.annotation;

import java.lang.annotation.*;

/**
 * 禁用自定义的统一API响应
 *
 * @author wangsiqian
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface DisableApiResponse {
}

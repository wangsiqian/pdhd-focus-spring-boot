package com.adhd.server.common.handler;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSONObject;
import com.adhd.server.common.ApiResponse;
import com.adhd.server.common.annotation.DisableApiResponse;
import com.adhd.server.common.annotation.EnableApiResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.Method;

/**
 * @author wangsiqian
 */
@RestControllerAdvice(annotations = EnableApiResponse.class)
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        if (ObjectUtil.isNotNull(returnType.getMethod())
                && ApiResponse.class.equals(returnType.getMethod().getReturnType())) {
            // 已经是 ApiResponse 了，不需要二次包装
            return false;
        }

        return ObjectUtil.isNull(returnType.getMethodAnnotation(DisableApiResponse.class));
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {

        ApiResponse<Object> apiResponse = ApiResponse.success(body);
        Method method = returnType.getMethod();
        if (ObjectUtil.isNotNull(method)
                && String.class.equals(method.getReturnType())) {
            // fix ApiResponse cannot be cast to java.lang.String
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return JSONObject.toJSONString(apiResponse);
        }

        return apiResponse;
    }
}

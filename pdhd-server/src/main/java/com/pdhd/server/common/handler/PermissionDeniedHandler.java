package com.pdhd.server.common.handler;

import cn.hutool.http.ContentType;
import com.alibaba.fastjson2.JSONObject;
import com.pdhd.server.common.ApiResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author wangsiqian
 */
@Component
@RestControllerAdvice
public class PermissionDeniedHandler implements AccessDeniedHandler, AuthenticationEntryPoint {
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        response.setContentType(ContentType.build(ContentType.JSON.getValue(), StandardCharsets.UTF_8));
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(Objects.requireNonNull(
                JSONObject.toJSONString(ApiResponse.unauthorized())).getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {

        response.setContentType(ContentType.build(ContentType.JSON.getValue(), StandardCharsets.UTF_8));
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(Objects.requireNonNull(
                JSONObject.toJSONString(ApiResponse.unauthorized())).getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
    }
}

package com.adhd.server.common.handler;

import cn.hutool.core.util.StrUtil;
import com.adhd.server.common.ApiResponse;
import com.adhd.server.common.ResponseCodeEnum;
import com.adhd.server.common.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author wangsiqian
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = AccessDeniedException.class)
    public ApiResponse<Boolean> accessDeniedExceptionHandler(AccessDeniedException accessDeniedException) {
        return ApiResponse.unauthorized();
    }

    @ExceptionHandler(value = ApiException.class)
    public ApiResponse<Object> apiExceptionHandler(ApiException apiException) {
        ApiResponse<Object> apiResponse = new ApiResponse<>();
        apiResponse.setSuccess(false);
        apiResponse.setCode(apiException.getCode());
        apiResponse.setMsg(apiException.getMessage());

        return apiResponse;
    }

    @ExceptionHandler(value = BindException.class)
    public ApiResponse<Object> bindExceptionHandler(BindException bindException) {
        ApiResponse<Object> apiResponse = new ApiResponse<>();
        apiResponse.setSuccess(false);
        apiResponse.setCode(ResponseCodeEnum.BAD_REQUEST.getCode());
        apiResponse.setMsg(
                bindException.getFieldErrors().stream()
                        .map(fieldError -> fieldError.getField() + fieldError.getDefaultMessage())
                        .collect(Collectors.joining(",")));

        return apiResponse;
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    public ApiResponse<Object> constraintViolationException(ConstraintViolationException exception) {
        ApiResponse<Object> apiResponse = new ApiResponse<>();
        String errorMessages =
                exception.getConstraintViolations().stream()
                        .map(violation ->
                                StrUtil.format("{} {}",
                                        StreamSupport.stream(violation.getPropertyPath().spliterator(), false)
                                                .reduce((first, second) -> second)
                                                .orElse(null),
                                        violation.getMessage()))
                        .collect(Collectors.joining(";"));

        apiResponse.setSuccess(false);
        apiResponse.setCode(ResponseCodeEnum.BAD_REQUEST.getCode());
        apiResponse.setMsg(errorMessages);

        return apiResponse;
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ApiResponse<Object> methodArgumentNotValidExceptionHandler(
            MethodArgumentNotValidException exception) {
        BindingResult bindingResult = exception.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        String errorMessages =
                fieldErrors.stream()
                        .map(error -> error.getField() + " " + error.getDefaultMessage())
                        .sorted()
                        .collect(Collectors.joining("; "));

        ApiResponse<Object> apiResponse = new ApiResponse<>();
        apiResponse.setSuccess(false);
        apiResponse.setMsg(errorMessages);
        apiResponse.setCode(ResponseCodeEnum.BAD_REQUEST.getCode());
        return apiResponse;
    }

    @ExceptionHandler(value = Exception.class)
    public Object exceptionHandler(Exception exception) {
        exception.printStackTrace();

        ApiResponse<Object> result = new ApiResponse<>();
        result.setSuccess(false);
        result.setCode(ResponseCodeEnum.INTERNAL_SERVER_ERROR.getCode());
        result.setMsg(ResponseCodeEnum.INTERNAL_SERVER_ERROR.getMessage());
        return result;
    }
}

package com.pdhd.server.common;

import lombok.Data;

/**
 * 接口通用返回结果
 *
 * @author wangsiqian
 */
@Data
public class ApiResponse<T> {
    private Integer code;

    private Boolean success;

    private String msg;

    private T data;

    /**
     * 返回请求成功的默认返回结果
     *
     * @return 成功的请求
     * @author wangsiqian
     */
    public static ApiResponse<Boolean> success() {
        ApiResponse<Boolean> apiResponse = new ApiResponse<>();
        apiResponse.setCode(ResponseCodeEnum.SUCCESS.getCode());
        apiResponse.setSuccess(true);
        apiResponse.setMsg(ResponseCodeEnum.SUCCESS.getMessage());
        return apiResponse;
    }

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> apiResponse = new ApiResponse<>();
        apiResponse.setCode(ResponseCodeEnum.SUCCESS.getCode());
        apiResponse.setSuccess(true);
        apiResponse.setMsg(ResponseCodeEnum.SUCCESS.getMessage());
        apiResponse.setData(data);
        return apiResponse;
    }

    /**
     * 权限验证失败时调用
     *
     * @return 设置为权限验证失败的返回 ApiResponse
     * @author wangsiqian
     */
    public static ApiResponse<Boolean> unauthorized() {
        ApiResponse<Boolean> apiResponse = new ApiResponse<>();
        apiResponse.setCode(ResponseCodeEnum.UNAUTHORIZED.getCode());
        apiResponse.setMsg(ResponseCodeEnum.UNAUTHORIZED.getMessage());
        apiResponse.setSuccess(false);
        apiResponse.setData(null);
        return apiResponse;
    }
}

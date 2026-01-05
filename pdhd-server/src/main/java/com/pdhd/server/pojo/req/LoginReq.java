package com.pdhd.server.pojo.req;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author wangsiqian
 */
@Data
public class LoginReq {
    @NotBlank(message = "账号不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}

package com.adhd.server.service;

import com.adhd.server.pojo.resp.UserDTO;
import com.adhd.server.pojo.req.LoginReq;
import com.adhd.server.pojo.resp.LoginDTO;

/**
 * @author wangsiqian
 */
public interface UserService {
    /**
     * 通过用户Id获取用户信息
     *
     * @param userId 用户Id
     * @return 用户信息
     * @author wangsiqian
     */
    UserDTO getById(Long userId);

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     * @author wangsiqian
     */
    UserDTO getCurrentUserInfo();

    /**
     * 登录
     *
     * @param req 登录请求
     * @return token
     * @author wangsiqian
     */
    LoginDTO login(LoginReq req);
}

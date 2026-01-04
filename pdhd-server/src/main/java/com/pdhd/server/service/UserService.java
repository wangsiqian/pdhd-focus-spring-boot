package com.pdhd.server.service;

import com.pdhd.server.pojo.resp.UserDTO;

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
}

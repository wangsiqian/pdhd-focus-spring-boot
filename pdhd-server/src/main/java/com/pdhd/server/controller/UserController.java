package com.pdhd.server.controller;

import com.pdhd.server.common.annotation.EnableApiResponse;
import com.pdhd.server.pojo.resp.UserDTO;
import com.pdhd.server.req.GetByIdReq;
import com.pdhd.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author wangsiqian
 */
@RequestMapping("/webApi/users")
@EnableApiResponse
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/info")
    public UserDTO getCurrentUserInfo() {
        return userService.getCurrentUserInfo();
    }

    @PostMapping("/getById")
    public UserDTO getById(@RequestBody GetByIdReq req) {
        return userService.getById(req.getId());
    }
}

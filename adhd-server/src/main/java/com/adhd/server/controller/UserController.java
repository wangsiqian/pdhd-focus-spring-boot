package com.adhd.server.controller;

import com.adhd.server.common.annotation.EnableApiResponse;
import com.adhd.server.pojo.req.LoginReq;
import com.adhd.server.pojo.resp.LoginDTO;
import com.adhd.server.pojo.resp.UserDTO;
import com.adhd.server.pojo.req.GetByIdReq;
import com.adhd.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

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

    @PostMapping("/login")
    public LoginDTO login(@Valid @RequestBody LoginReq req) {
        return userService.login(req);
    }
}

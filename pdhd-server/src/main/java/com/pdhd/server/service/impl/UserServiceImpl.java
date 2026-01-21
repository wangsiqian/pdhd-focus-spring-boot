package com.pdhd.server.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.pdhd.server.common.exception.ApiException;
import com.pdhd.server.common.util.BeanUtils;
import com.pdhd.server.common.util.ContextUtils;
import com.pdhd.server.common.util.JwtUtils;
import com.pdhd.server.common.util.PasswordUtils;
import com.pdhd.server.dao.entity.User;
import com.pdhd.server.dao.repository.UserRepository;
import com.pdhd.server.exception.UserExceptionEnum;
import com.pdhd.server.manager.UserManager;
import com.pdhd.server.pojo.req.LoginReq;
import com.pdhd.server.pojo.resp.LoginDTO;
import com.pdhd.server.pojo.resp.UserDTO;
import com.pdhd.server.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author wangsiqian
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserManager userManager;

    @Override
    public UserDTO getById(Long userId) {
        User user = userRepository.getById(userId);
        if (Objects.isNull(user)) {
            log.info("账号不存在：{}", userId);
            throw new ApiException(UserExceptionEnum.USER_NOT_FOUND);
        }

        return BeanUtils.copy(user, UserDTO::new);
    }

    @Override
    public UserDTO getCurrentUserInfo() {
        User user = ContextUtils.currentUser();
        return BeanUtils.copy(user, UserDTO::new);
    }

    @Override
    public LoginDTO login(LoginReq req) {
        log.info("用户登录，username：{}", req.getUsername());

        User user = userRepository.lambdaQuery()
                .eq(User::getUsername, req.getUsername())
                .one();
        if (ObjectUtil.isNull(user) || !PasswordUtils.matches(req.getPassword(), user.getPassword())) {
            log.info("账号或密码错误，username：{}", req.getUsername());
            throw new ApiException(UserExceptionEnum.ACCOUNT_OR_PASSWORD_WRONG);
        }

        String token = JwtUtils.generateToken(user);
        userManager.saveToken(user.getId(), token);
        return LoginDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .token(token)
                .build();
    }
}

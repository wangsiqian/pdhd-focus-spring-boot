package com.pdhd.server.service.impl;

import com.pdhd.server.common.exception.ApiException;
import com.pdhd.server.common.util.BeanUtils;
import com.pdhd.server.common.util.ContextUtils;
import com.pdhd.server.dao.entity.User;
import com.pdhd.server.dao.repository.UserRepository;
import com.pdhd.server.exception.UserExceptionEnum;
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
}

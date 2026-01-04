package com.pdhd.server.dao.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pdhd.server.dao.entity.User;
import com.pdhd.server.dao.mapper.UserMapper;
import org.springframework.stereotype.Component;

/**
 * @author wangsiqian
 */
@Component
public class UserRepository extends ServiceImpl<UserMapper, User> {
}

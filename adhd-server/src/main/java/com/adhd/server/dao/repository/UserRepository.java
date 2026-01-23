package com.adhd.server.dao.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.adhd.server.dao.entity.User;
import com.adhd.server.dao.mapper.UserMapper;
import org.springframework.stereotype.Component;

/**
 * @author wangsiqian
 */
@Component
public class UserRepository extends ServiceImpl<UserMapper, User> {
}

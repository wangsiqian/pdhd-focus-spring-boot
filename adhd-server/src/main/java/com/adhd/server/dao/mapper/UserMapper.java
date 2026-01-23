package com.adhd.server.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.adhd.server.dao.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author wangsiqian
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}

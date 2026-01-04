package com.pdhd.server.dao.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pdhd.server.dao.entity.Goal;
import com.pdhd.server.dao.mapper.GoalMapper;
import org.springframework.stereotype.Component;

/**
 * @author pdhd
 */
@Component
public class GoalRepository extends ServiceImpl<GoalMapper, Goal> {
}
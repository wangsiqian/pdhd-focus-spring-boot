package com.adhd.server.dao.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.adhd.server.dao.entity.Goal;
import com.adhd.server.dao.mapper.GoalMapper;
import org.springframework.stereotype.Component;

/**
 * @author adhd
 */
@Component
public class GoalRepository extends ServiceImpl<GoalMapper, Goal> {
}
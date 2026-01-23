package com.adhd.server.dao.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.adhd.server.dao.entity.Activity;
import com.adhd.server.dao.mapper.ActivityMapper;
import org.springframework.stereotype.Component;

/**
 * @author adhd
 */
@Component
public class ActivityRepository extends ServiceImpl<ActivityMapper, Activity> {
}
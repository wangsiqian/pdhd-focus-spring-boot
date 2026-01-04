package com.pdhd.server.dao.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pdhd.server.dao.entity.Activity;
import com.pdhd.server.dao.mapper.ActivityMapper;
import org.springframework.stereotype.Component;

/**
 * @author pdhd
 */
@Component
public class ActivityRepository extends ServiceImpl<ActivityMapper, Activity> {
}
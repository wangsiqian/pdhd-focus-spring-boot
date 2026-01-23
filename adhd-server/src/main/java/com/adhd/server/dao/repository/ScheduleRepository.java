package com.adhd.server.dao.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.adhd.server.dao.entity.Schedule;
import com.adhd.server.dao.mapper.ScheduleMapper;
import org.springframework.stereotype.Component;

/**
 * @author adhd
 */
@Component
public class ScheduleRepository extends ServiceImpl<ScheduleMapper, Schedule> {
}
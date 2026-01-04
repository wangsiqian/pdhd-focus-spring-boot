package com.pdhd.server.dao.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pdhd.server.dao.entity.Schedule;
import com.pdhd.server.dao.mapper.ScheduleMapper;
import org.springframework.stereotype.Component;

/**
 * @author pdhd
 */
@Component
public class ScheduleRepository extends ServiceImpl<ScheduleMapper, Schedule> {
}
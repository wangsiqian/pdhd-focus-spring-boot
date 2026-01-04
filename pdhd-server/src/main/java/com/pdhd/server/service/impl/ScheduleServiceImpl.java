package com.pdhd.server.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.pdhd.server.common.enums.RepeatRuleEnum;
import com.pdhd.server.common.exception.ApiException;
import com.pdhd.server.common.util.ContextUtils;
import com.pdhd.server.dao.entity.Schedule;
import com.pdhd.server.dao.repository.ScheduleRepository;
import com.pdhd.server.exception.ScheduleExceptionEnum;
import com.pdhd.server.pojo.req.ListPLanReq;
import com.pdhd.server.pojo.req.ListScheduleReq;
import com.pdhd.server.pojo.req.ScheduleReq;
import com.pdhd.server.pojo.resp.PlanDTO;
import com.pdhd.server.pojo.resp.ScheduleDTO;
import com.pdhd.server.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author pdhd
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {
    private final ScheduleRepository scheduleRepository;

    @Override
    public ScheduleDTO getById(Long id) {
        Schedule schedule = scheduleRepository.getById(id);
        if (Objects.isNull(schedule)) {
            log.info("计划不存在：{}", id);
            throw new ApiException(ScheduleExceptionEnum.SCHEDULE_NOT_FOUND);
        }

        // 检查计划是否属于当前用户
        Long currentUserId = ContextUtils.currentUser().getId();
        if (!Objects.equals(schedule.getUserId(), currentUserId)) {
            log.info("用户无权限访问此计划：{}", id);
            throw new ApiException(ScheduleExceptionEnum.SCHEDULE_NOT_FOUND);
        }

        return convertToDTO(schedule);
    }

    @Override
    public List<ScheduleDTO> list(ListScheduleReq req) {
        Long currentUserId = ContextUtils.currentUser().getId();

        // 查询临时计划的数据
        List<Schedule> schedules = scheduleRepository.lambdaQuery()
                .eq(Schedule::getUserId, currentUserId)
                .eq(Objects.nonNull(req.getGoalId()), Schedule::getGoalId, req.getGoalId())
                .ge(Objects.nonNull(req.getStartTime()), Schedule::getStartTime, req.getStartTime())
                .le(Objects.nonNull(req.getEndTime()), Schedule::getEndTime, req.getEndTime())
                .eq(Schedule::getRepeatRuleType, RepeatRuleEnum.NONE)
                .list();

        // 有重复规则的计划，需要根据规则过滤
        List<Schedule> repeatSchedules = scheduleRepository.lambdaQuery()
                .eq(Schedule::getUserId, currentUserId)
                .eq(Objects.nonNull(req.getGoalId()), Schedule::getGoalId, req.getGoalId())
                .eq(Schedule::getRepeatRuleType, RepeatRuleEnum.CUSTOM)
                .list();
        schedules.addAll(filterSchedulesByRule(repeatSchedules, req.getStartTime(), req.getEndTime()));

        // 根据fullDetail参数决定是否返回content字段
        return schedules.stream()
                .map(schedule -> convertToDTO(schedule, req.getFullDetail()))
                .sorted(Comparator.comparing(ScheduleDTO::getCreatedAt))
                .collect(Collectors.toList());
    }

    private static List<Schedule> filterSchedulesByRule(List<Schedule> repeatSchedules, LocalDateTime startTime, LocalDateTime endTime) {
        Set<DayOfWeek> daysOfWeek = Sets.newHashSet();
        while (LocalDateTimeUtil.between(startTime, endTime).toDays() >= 0) {
            daysOfWeek.add(startTime.getDayOfWeek());
            startTime = LocalDateTimeUtil.offset(startTime, 1, ChronoUnit.DAYS);
        }

        List<Schedule> result = Lists.newArrayList();
        for (Schedule schedule : repeatSchedules) {
            JSONObject repeatRuleConfig = JSONObject.parse(schedule.getRepeatRuleConfig());
            if (CollUtil.containsAny(repeatRuleConfig.getList("daysOfWeek", DayOfWeek.class), daysOfWeek)) {
                result.add(schedule);
            }
        }
        return result;
    }

    @Override
    public ScheduleDTO upsert(ScheduleReq scheduleReq) {
        Long currentUserId = ContextUtils.currentUser().getId();
        if (scheduleReq.getId() != null) {
            // 更新操作
            Schedule existingSchedule = scheduleRepository.getById(scheduleReq.getId());
            if (Objects.isNull(existingSchedule)) {
                log.info("计划不存在：{}", scheduleReq.getId());
                throw new ApiException(ScheduleExceptionEnum.SCHEDULE_NOT_FOUND);
            }

            // 检查计划是否属于当前用户
            if (!Objects.equals(existingSchedule.getUserId(), currentUserId)) {
                log.info("用户无权限修改此计划：{}", scheduleReq.getId());
                throw new ApiException(ScheduleExceptionEnum.SCHEDULE_NOT_FOUND);
            }

            Schedule schedule = convertToEntity(scheduleReq, currentUserId);
            schedule.setId(scheduleReq.getId());
            schedule.setUserId(currentUserId); // 确保不会更改用户ID
            scheduleRepository.updateById(schedule);
            return convertToDTO(schedule);
        } else {
            // 创建操作
            Schedule schedule = convertToEntity(scheduleReq, currentUserId);
            schedule.setUserId(currentUserId);
            scheduleRepository.save(schedule);
            return convertToDTO(schedule);
        }
    }

    @Override
    public void delete(Long id) {
        Schedule existingSchedule = scheduleRepository.getById(id);
        if (Objects.isNull(existingSchedule)) {
            log.info("计划不存在：{}", id);
            throw new ApiException(ScheduleExceptionEnum.SCHEDULE_NOT_FOUND);
        }

        // 检查计划是否属于当前用户
        Long currentUserId = ContextUtils.currentUser().getId();
        if (!Objects.equals(existingSchedule.getUserId(), currentUserId)) {
            log.info("用户无权限删除此计划：{}", id);
            throw new ApiException(ScheduleExceptionEnum.SCHEDULE_NOT_FOUND);
        }

        scheduleRepository.removeById(id);
    }

    /**
     * 获取计划
     */
    @Override
    public List<PlanDTO> plan(ListPLanReq req) {
        List<ScheduleDTO> schedules = list(ListScheduleReq.builder()
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .build());
        if (CollectionUtil.isEmpty(schedules)) {
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }

    private Schedule convertToEntity(ScheduleReq scheduleReq, Long userId) {
        return Schedule.builder()
                .id(scheduleReq.getId())
                .title(scheduleReq.getTitle())
                .content(scheduleReq.getContent())
                .type(scheduleReq.getType())
                .zone(scheduleReq.getZone())
                .goalId(scheduleReq.getGoalId())
                .startTime(scheduleReq.getStartTime())
                .endTime(scheduleReq.getEndTime())
                .repeatRuleType(scheduleReq.getRepeatRule() != null ? scheduleReq.getRepeatRule() : RepeatRuleEnum.NONE)
                .repeatRuleConfig(scheduleReq.getCustomDays())
                .groupId(scheduleReq.getGroupId())
                .userId(userId)
                .build();
    }

    private ScheduleDTO convertToDTO(Schedule schedule) {
        return convertToDTO(schedule, true); // 默认返回完整信息
    }

    private ScheduleDTO convertToDTO(Schedule schedule, Boolean fullDetail) {
        ScheduleDTO dto = new ScheduleDTO();
        dto.setId(schedule.getId());
        dto.setTitle(schedule.getTitle());

        // 根据fullDetail参数决定是否返回content字段
        if (fullDetail != null && fullDetail) {
            dto.setContent(schedule.getContent());
        }

        dto.setType(schedule.getType());
        dto.setZone(schedule.getZone());
        dto.setGoalId(schedule.getGoalId());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        dto.setRepeatRule(schedule.getRepeatRuleType());
        dto.setCustomDays(schedule.getRepeatRuleConfig());
        dto.setGroupId(schedule.getGroupId());
        dto.setUserId(schedule.getUserId());
        dto.setCreatedAt(schedule.getCreatedAt());
        return dto;
    }
}

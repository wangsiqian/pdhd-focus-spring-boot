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
import java.time.LocalTime;
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

        // 1. 查询单次计划的数据 (使用 startDateTime 和 endDateTime)
        // 查询条件：计划的开始时间 <= 范围结束时间 且 计划的结束时间 >= 范围开始时间
        List<Schedule> singleSchedules = scheduleRepository.lambdaQuery()
                .eq(Schedule::getUserId, currentUserId)
                .eq(Objects.nonNull(req.getGoalId()), Schedule::getGoalId, req.getGoalId())
                .le(Schedule::getStartDateTime, req.getEndDateTime())
                .ge(Schedule::getEndDateTime, req.getStartDateTime())
                .eq(Schedule::getRepeatRuleType, RepeatRuleEnum.NONE)
                .list();

        List<ScheduleDTO> result = singleSchedules.stream()
                .map(schedule -> convertToDTO(schedule, req.getFullDetail()))
                .collect(Collectors.toList());

        // 2. 查询循环计划的数据 (使用 startTime 和 endTime)
        List<Schedule> repeatSchedules = scheduleRepository.lambdaQuery()
                .eq(Schedule::getUserId, currentUserId)
                .eq(Objects.nonNull(req.getGoalId()), Schedule::getGoalId, req.getGoalId())
                .eq(Schedule::getRepeatRuleType, RepeatRuleEnum.CUSTOM)
                .list();

        // 3. 展开循环计划为具体实例
        result.addAll(expandRepeatSchedules(repeatSchedules, req.getStartDateTime(), req.getEndDateTime(),
                req.getFullDetail()));

        // 4. 排序
        return result.stream()
                .sorted(Comparator.comparing(ScheduleDTO::getStartDateTime))
                .collect(Collectors.toList());
    }

    private List<ScheduleDTO> expandRepeatSchedules(List<Schedule> repeatSchedules, LocalDateTime queryStart,
            LocalDateTime queryEnd, Boolean fullDetail) {
        List<ScheduleDTO> instances = new ArrayList<>();

        // 遍历每一天
        long days = LocalDateTimeUtil.between(queryStart, queryEnd).toDays();
        // 如果结束时间是当天的 00:00:00，可能是要查到前一天结束，这里简单处理，包含 start 到 end 的每一天
        // 更严谨的做法是按天迭代

        for (int i = 0; i <= days + 1; i++) { // +1 确保覆盖最后一天（如果时间有重叠）
            LocalDateTime currentDate = queryStart.plusDays(i).truncatedTo(ChronoUnit.DAYS);
            // 如果当前迭代的日期已经超过了查询结束时间，停止。（注意处理边界）
            if (currentDate.isAfter(queryEnd)) {
                break;
            }

            DayOfWeek currentDayOfWeek = currentDate.getDayOfWeek();

            for (Schedule schedule : repeatSchedules) {
                JSONObject repeatRuleConfig = JSONObject.parse(schedule.getRepeatRuleConfig());
                List<DayOfWeek> ruleDays = repeatRuleConfig.getList("daysOfWeek", DayOfWeek.class);

                if (CollUtil.contains(ruleDays, currentDayOfWeek)) {
                    // 结合 Date 和 Time 生成 DateTime
                    LocalDateTime instanceStart = LocalDateTime.of(currentDate.toLocalDate(), schedule.getStartTime());
                    LocalDateTime instanceEnd = LocalDateTime.of(currentDate.toLocalDate(), schedule.getEndTime());

                    // 处理跨天情况：如果结束时间小于开始时间，说明跨天了，结束日期需要+1天
                    if (schedule.getEndTime().isBefore(schedule.getStartTime())) {
                        instanceEnd = instanceEnd.plusDays(1);
                    }

                    // 只有当实例的时间段与查询时间段有交集时才添加
                    // 实例结束 > 查询开始 && 实例开始 < 查询结束
                    if (instanceEnd.isAfter(queryStart) && instanceStart.isBefore(queryEnd)) {
                        ScheduleDTO dto = convertToDTO(schedule, fullDetail);
                        // 覆盖为具体的实例时间 (LocalDateTime)
                        dto.setStartDateTime(instanceStart);
                        dto.setEndDateTime(instanceEnd);
                        // 同时保留规则时间 (LocalTime)
                        dto.setStartTime(schedule.getStartTime());
                        dto.setEndTime(schedule.getEndTime());
                        instances.add(dto);
                    }
                }
            }
        }
        return instances;
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
                .startDateTime(req.getStartTime())
                .endDateTime(req.getEndTime())
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
                .startDateTime(scheduleReq.getStartDateTime())
                .endDateTime(scheduleReq.getEndDateTime())
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

        // 默认设置为单次计划的时间
        if (RepeatRuleEnum.NONE.equals(schedule.getRepeatRuleType())) {
            dto.setStartDateTime(schedule.getStartDateTime());
            dto.setEndDateTime(schedule.getEndDateTime());
        } else {
            // 循环计划：设置规则时间
            dto.setStartTime(schedule.getStartTime());
            dto.setEndTime(schedule.getEndTime());
            // 循环计划的基础定义中，StartDateTime/EndDateTime 可能为空，或者设为第一次发生的时间？
            // 暂时保持与 Entity 一致
            dto.setStartDateTime(schedule.getStartDateTime());
            dto.setEndDateTime(schedule.getEndDateTime());
        }
        dto.setRepeatRule(schedule.getRepeatRuleType());
        dto.setCustomDays(schedule.getRepeatRuleConfig());
        dto.setGroupId(schedule.getGroupId());
        dto.setUserId(schedule.getUserId());
        dto.setCreatedAt(schedule.getCreatedAt());
        return dto;
    }
}

package com.pdhd.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.BooleanUtil;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Lists;
import com.pdhd.server.common.enums.RepeatRuleEnum;
import com.pdhd.server.common.exception.ApiException;
import com.pdhd.server.common.util.ContextUtils;
import com.pdhd.server.dao.entity.Activity;
import com.pdhd.server.dao.entity.Schedule;
import com.pdhd.server.dao.repository.ActivityRepository;
import com.pdhd.server.dao.repository.ScheduleRepository;
import com.pdhd.server.exception.ScheduleExceptionEnum;
import com.pdhd.server.pojo.req.*;
import com.pdhd.server.pojo.resp.PlanDTO;
import com.pdhd.server.pojo.resp.ScheduleDTO;
import com.pdhd.server.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author pdhd
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final ActivityRepository activityRepository;

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
        log.debug("Get schedule list, userId: {}, req: {}", currentUserId, req);

        // 1. 查询单次计划的数据
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

        // 2. 查询循环计划的数据
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
        for (int i = 0; i <= days + 1; i++) {
            LocalDateTime currentDate = queryStart.plusDays(i).truncatedTo(ChronoUnit.DAYS);
            if (currentDate.isAfter(queryEnd)) {
                break;
            }

            DayOfWeek currentDayOfWeek = currentDate.getDayOfWeek();

            for (Schedule schedule : repeatSchedules) {
                try {
                    JSONObject repeatRuleConfig = JSONObject.parse(schedule.getRepeatRuleConfig());
                    if (repeatRuleConfig == null)
                        continue;

                    List<DayOfWeek> ruleDays = repeatRuleConfig.getList("daysOfWeek", DayOfWeek.class);
                    if (CollUtil.contains(ruleDays, currentDayOfWeek)) {
                        // 结合 Date 和 Time 生成 DateTime
                        LocalDateTime instanceStart = LocalDateTime.of(currentDate.toLocalDate(),
                                schedule.getStartTime());
                        LocalDateTime instanceEnd = LocalDateTime.of(currentDate.toLocalDate(), schedule.getEndTime());

                        // 处理跨天 (结束时间小于开始时间)
                        if (schedule.getEndTime().isBefore(schedule.getStartTime())) {
                            instanceEnd = instanceEnd.plusDays(1);
                        }

                        // 判断时间段交集：实例结束 > 查询开始 && 实例开始 < 查询结束
                        if (instanceEnd.isAfter(queryStart) && instanceStart.isBefore(queryEnd)) {
                            ScheduleDTO dto = convertToDTO(schedule, fullDetail);
                            dto.setStartDateTime(instanceStart);
                            dto.setEndDateTime(instanceEnd);
                            // 保留规则时间
                            dto.setStartTime(schedule.getStartTime());
                            dto.setEndTime(schedule.getEndTime());
                            instances.add(dto);
                        }
                    }
                } catch (Exception e) {
                    log.error("Parse repeat rule failed, scheduleId: {}", schedule.getId(), e);
                }
            }
        }
        return instances;
    }

    @Override
    public ScheduleDTO upsert(ScheduleReq scheduleReq) {
        Long currentUserId = ContextUtils.currentUser().getId();
        log.info("Upsert schedule, userId: {}, req: {}", currentUserId, scheduleReq);

        if (scheduleReq.getId() != null) {
            // 更新操作
            Schedule existingSchedule = scheduleRepository.getById(scheduleReq.getId());
            if (Objects.isNull(existingSchedule)) {
                log.warn("Schedule not found: {}", scheduleReq.getId());
                throw new ApiException(ScheduleExceptionEnum.SCHEDULE_NOT_FOUND);
            }

            // 检查计划是否属于当前用户
            if (!Objects.equals(existingSchedule.getUserId(), currentUserId)) {
                log.warn("Permission denied for schedule: {}, currentUserId: {}", scheduleReq.getId(), currentUserId);
                throw new ApiException(ScheduleExceptionEnum.SCHEDULE_NOT_FOUND);
            }

            Schedule schedule = convertToEntity(scheduleReq, currentUserId);
            schedule.setId(scheduleReq.getId());
            schedule.setUserId(currentUserId);
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
        Long currentUserId = ContextUtils.currentUser().getId();
        log.info("Delete schedule: {}, userId: {}", id, currentUserId);

        Schedule existingSchedule = scheduleRepository.getById(id);
        if (Objects.isNull(existingSchedule)) {
            log.warn("Schedule not found: {}", id);
            throw new ApiException(ScheduleExceptionEnum.SCHEDULE_NOT_FOUND);
        }

        if (!Objects.equals(existingSchedule.getUserId(), currentUserId)) {
            log.warn("Permission denied for schedule: {}, currentUserId: {}", id, currentUserId);
            throw new ApiException(ScheduleExceptionEnum.SCHEDULE_NOT_FOUND);
        }

        scheduleRepository.removeById(id);
    }

    /**
     * 获取计划
     */
    @Override
    public List<PlanDTO> plan(ListPLanReq req) {
        log.debug("Get plan list, req: {}", req);
        List<ScheduleDTO> schedules = list(ListScheduleReq.builder()
                .startDateTime(req.getStartDateTime())
                .endDateTime(req.getEndDateTime())
                .build());

        if (CollectionUtil.isEmpty(schedules)) {
            return Collections.emptyList();
        }

        Map<LocalDate, List<ScheduleDTO>> map = new HashMap<>();

        for (ScheduleDTO schedule : schedules) {
            LocalDateTime startDateTime = schedule.getStartDateTime();
            LocalDateTime endDateTime = schedule.getEndDateTime();

            LocalDate startDate = startDateTime.toLocalDate();
            LocalDate endDate = endDateTime.toLocalDate();

            // 遍历该计划跨越的每一天
            long days = LocalDateTimeUtil.between(startDate.atStartOfDay(), endDate.atStartOfDay()).toDays();

            for (int i = 0; i <= days; i++) {
                LocalDate currentDate = startDate.plusDays(i);

                LocalDateTime splitStart = startDateTime;
                if (currentDate.isAfter(startDate)) {
                    // 非第一天，开始时间为 00:00:00
                    splitStart = currentDate.atStartOfDay();
                }

                LocalDateTime splitEnd = endDateTime;
                if (currentDate.isBefore(endDate)) {
                    // 非最后一天，结束时间为 23:59:59
                    splitEnd = LocalDateTime.of(currentDate, LocalTime.MAX);
                }

                ScheduleDTO newDto = new ScheduleDTO();
                BeanUtil.copyProperties(schedule, newDto);
                newDto.setStartDateTime(splitStart);
                newDto.setEndDateTime(splitEnd);

                map.computeIfAbsent(currentDate, k -> Lists.newArrayList()).add(newDto);
            }
        }

        // 转换为 PlanDTO 列表并排序
        return map.entrySet().stream()
                .map(entry -> {
                    PlanDTO planDTO = new PlanDTO();
                    planDTO.setDate(entry.getKey());
                    planDTO.setSchedules(entry.getValue().stream()
                            .sorted(Comparator.comparing(ScheduleDTO::getStartDateTime))
                            .collect(Collectors.toList()));
                    return planDTO;
                })
                .sorted(Comparator.comparing(PlanDTO::getDate))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(CompleteScheduleReq req) {
        BatchCompleteScheduleReq batchReq = new BatchCompleteScheduleReq();
        batchReq.setSchedules(Collections.singletonList(req));
        batchComplete(batchReq);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchComplete(BatchCompleteScheduleReq req) {
        Long currentUserId = ContextUtils.currentUser().getId();
        log.info("Batch complete schedules, userId: {}, count: {}", currentUserId,
                req.getSchedules() != null ? req.getSchedules().size() : 0);

        if (CollUtil.isEmpty(req.getSchedules())) {
            return;
        }

        // 1. 获取所有 Schedule ID并查询
        Set<Long> scheduleIds = req.getSchedules().stream()
                .map(CompleteScheduleReq::getScheduleId)
                .collect(Collectors.toSet());

        List<Schedule> schedules = scheduleRepository.lambdaQuery()
                .in(Schedule::getId, scheduleIds)
                .eq(Schedule::getUserId, currentUserId)
                .list();
        Map<Long, Schedule> scheduleMap = schedules.stream()
                .collect(Collectors.toMap(Schedule::getId, Function.identity()));

        // 用于防止本次请求内部的重复（比如请求里同一个计划完成两次）以及后续DB查重
        Set<String> checkKeys = new HashSet<>();
        List<Activity> pendingActivities = new ArrayList<>();

        for (CompleteScheduleReq item : req.getSchedules()) {
            Schedule schedule = scheduleMap.get(item.getScheduleId());
            if (schedule == null) {
                log.warn("Schedule not found: {}", item.getScheduleId());
                throw new ApiException(ScheduleExceptionEnum.SCHEDULE_NOT_FOUND);
            }
            if (!Objects.equals(schedule.getUserId(), currentUserId)) {
                log.warn("Permission denied for schedule: {}, currentUserId: {}", item.getScheduleId(), currentUserId);
                throw new ApiException(ScheduleExceptionEnum.SCHEDULE_NOT_FOUND);
            }

            // 时间处理逻辑
            LocalDateTime startDateTime = item.getStartDateTime();
            LocalDateTime endDateTime = item.getEndDateTime();

            if (RepeatRuleEnum.NONE == schedule.getRepeatRuleType()) {
                // 单次计划：使用计划设定的时间
                startDateTime = schedule.getStartDateTime();
                endDateTime = schedule.getEndDateTime();
            }

            if (startDateTime == null || endDateTime == null) {
                log.warn("Recurring schedule complete requires start/end time. req: {}", item);
                throw new ApiException(ScheduleExceptionEnum.SCHEDULE_NOT_FOUND);
            }

            // 时间校验：只能完成现在和过去的任务
            if (startDateTime.isAfter(LocalDateTime.now())) {
                log.warn("Cannot complete future schedule: {}", item);
                continue;
            }

            // 构造 Key 检查是否已包含在本次待保存列表中
            String key = buildActivityKey(schedule.getId(), startDateTime);
            if (checkKeys.contains(key)) {
                continue;
            }
            checkKeys.add(key);

            Activity activity = Activity.builder()
                    .scheduleId(schedule.getId())
                    .title(schedule.getTitle())
                    .content(schedule.getContent())
                    .type(schedule.getType())
                    .zone(schedule.getZone())
                    .goalId(schedule.getGoalId())
                    .startTime(startDateTime)
                    .endTime(endDateTime)
                    .userId(currentUserId)
                    .build();

            pendingActivities.add(activity);
        }

        if (CollUtil.isEmpty(pendingActivities)) {
            return;
        }

        Set<LocalDateTime> startTimes = pendingActivities.stream()
                .map(Activity::getStartTime)
                .collect(Collectors.toSet());

        List<Activity> existingActivities = activityRepository.lambdaQuery()
                .in(Activity::getScheduleId, scheduleIds)
                .in(Activity::getStartTime, startTimes)
                .eq(Activity::getUserId, currentUserId)
                .list();

        Set<String> existingKeys = existingActivities.stream()
                .map(this::buildActivityKey)
                .collect(Collectors.toSet());

        List<Activity> finalSaveList = pendingActivities.stream()
                .filter(a -> !existingKeys.contains(buildActivityKey(a)))
                .collect(Collectors.toList());

        if (CollUtil.isNotEmpty(finalSaveList)) {
            activityRepository.saveBatch(finalSaveList);
            log.info("Batch save activities success, count: {}", finalSaveList.size());
        }
    }

    @Override
    public void uncomplete(UncompleteScheduleReq req) {
        Long currentUserId = ContextUtils.currentUser().getId();
        log.info("Uncomplete schedule, userId: {}, req: {}", currentUserId, req);

        activityRepository.lambdaUpdate()
                .eq(Activity::getScheduleId, req.getScheduleId())
                .eq(Activity::getStartTime, req.getStartDateTime())
                .eq(Activity::getUserId, currentUserId)
                .set(Activity::getIsDelete, Boolean.TRUE)
                .update();
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

    private String buildActivityKey(Activity activity) {
        return buildActivityKey(activity.getScheduleId(), activity.getStartTime());
    }

    private String buildActivityKey(Long scheduleId, LocalDateTime startDateTime) {
        return scheduleId + "_" + startDateTime.toString();
    }

    private ScheduleDTO convertToDTO(Schedule schedule) {
        return convertToDTO(schedule, true); // 默认返回完整信息
    }

    private ScheduleDTO convertToDTO(Schedule schedule, Boolean fullDetail) {
        ScheduleDTO dto = new ScheduleDTO();
        dto.setId(schedule.getId());
        dto.setTitle(schedule.getTitle());

        // 根据fullDetail参数决定是否返回content字段
        if (BooleanUtil.isTrue(fullDetail)) {
            dto.setContent(schedule.getContent());
        }

        dto.setType(schedule.getType());
        dto.setZone(schedule.getZone());
        dto.setGoalId(schedule.getGoalId());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        dto.setStartDateTime(schedule.getStartDateTime());
        dto.setEndDateTime(schedule.getEndDateTime());
        dto.setRepeatRule(schedule.getRepeatRuleType());
        dto.setCustomDays(schedule.getRepeatRuleConfig());
        dto.setGroupId(schedule.getGroupId());
        dto.setUserId(schedule.getUserId());
        dto.setCreatedAt(schedule.getCreatedAt());
        return dto;
    }
}

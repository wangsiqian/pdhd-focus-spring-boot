package com.adhd.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.adhd.server.pojo.req.*;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.adhd.server.common.enums.RepeatRuleEnum;
import com.adhd.server.common.exception.ApiException;
import com.adhd.server.common.util.ContextUtils;
import com.adhd.server.dao.entity.Activity;
import com.adhd.server.dao.entity.Schedule;
import com.adhd.server.dao.repository.ActivityRepository;
import com.adhd.server.dao.repository.ScheduleRepository;
import com.adhd.server.exception.ScheduleExceptionEnum;
import com.adhd.server.pojo.req.*;
import com.adhd.server.pojo.resp.PlanDTO;
import com.adhd.server.pojo.resp.ScheduleDTO;
import com.adhd.server.service.ScheduleService;
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
 * @author adhd
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {
    private static final List<String> DAILY_RULE_DAYS = Lists.newArrayList(
            DayOfWeek.MONDAY.name(),
            DayOfWeek.TUESDAY.name(),
            DayOfWeek.WEDNESDAY.name(),
            DayOfWeek.THURSDAY.name(),
            DayOfWeek.FRIDAY.name(),
            DayOfWeek.SATURDAY.name(),
            DayOfWeek.SUNDAY.name()
    );
    private static final List<String> WEEKDAY_RULE_DAYS = Lists.newArrayList(
            DayOfWeek.MONDAY.name(),
            DayOfWeek.TUESDAY.name(),
            DayOfWeek.WEDNESDAY.name(),
            DayOfWeek.THURSDAY.name(),
            DayOfWeek.FRIDAY.name()
    );
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
                .ne(Schedule::getRepeatRuleType, RepeatRuleEnum.NONE)
                .list();

        // 3. 展开循环计划为具体实例
        result.addAll(expandRepeatSchedules(repeatSchedules, req.getStartDateTime(), req.getEndDateTime(),
                req.getFullDetail()));

        // 4. 补充完成状态
        fillCompletedStatus(result, currentUserId);

        // 5. 补充是否恢复状态
        fillResumingStatus(result, currentUserId);

        // 6. 排序
        return result.stream()
                .sorted(Comparator.comparing(ScheduleDTO::getStartDateTime))
                .collect(Collectors.toList());
    }

    /**
     * 补充当天计划是否完成
     */
    private void fillCompletedStatus(List<ScheduleDTO> schedules, Long currentUserId) {
        if (CollUtil.isEmpty(schedules)) {
            return;
        }
        Set<Long> scheduleIds = Sets.newHashSetWithExpectedSize(schedules.size());
        Set<LocalDateTime> startTimes = Sets.newHashSetWithExpectedSize(schedules.size());
        schedules.forEach(dto -> {
            scheduleIds.add(dto.getId());
            startTimes.add(dto.getStartDateTime());
        });
        List<Activity> activities = activityRepository.lambdaQuery()
                .in(Activity::getScheduleId, scheduleIds)
                .in(Activity::getStartDateTime, startTimes)
                .eq(Activity::getUserId, currentUserId)
                .list();
        Set<String> completedKeys = activities.stream()
                .map(activity -> buildActivityKey(activity.getScheduleId(), activity.getStartDateTime()))
                .collect(Collectors.toSet());
        schedules.forEach(dto -> dto.setCompleted(completedKeys.contains(
                buildActivityKey(dto.getId(), dto.getStartDateTime())
        )));
    }

    private void fillResumingStatus(List<ScheduleDTO> schedules, Long currentUserId) {
        if (CollUtil.isEmpty(schedules)) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        Activity tempActivity = activityRepository.lambdaQuery()
                .eq(Activity::getUserId, currentUserId)
                .le(Activity::getStartDateTime, now)
                .ge(Activity::getEndDateTime, now)
                .eq(Activity::getScheduleId, 0)
                .last("limit 1")
                .one();
        boolean hasTempActivity = tempActivity != null;
        schedules.forEach(dto -> {
            if (!hasTempActivity) {
                dto.setIsResuming(Boolean.FALSE);
                return;
            }
            boolean isRunning = !dto.getStartDateTime().isAfter(now) && dto.getEndDateTime().isAfter(now);
            dto.setIsResuming(isRunning);
        });
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

                    List<DayOfWeek> ruleDays = parseRepeatRuleDays(repeatRuleConfig);
                    if (CollUtil.contains(ruleDays, currentDayOfWeek)) {
                        // 结合 Date 和 Time 生成 DateTime
                        LocalDateTime instanceStart = LocalDateTime.of(currentDate.toLocalDate(),
                                schedule.getStartTime());
                        LocalDateTime instanceEnd = LocalDateTime.of(currentDate.toLocalDate(), schedule.getEndTime());

                        // 处理跨天 (结束时间小于开始时间)
                        if (schedule.getEndTime().isBefore(schedule.getStartTime())) {
                            instanceEnd = instanceEnd.plusDays(1);
                        }

                        if (isInstanceInQueryRange(instanceStart, instanceEnd, queryStart, queryEnd, schedule.getCreatedAt())) {
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

    /**
     * 判断实例是否在查询范围内
     * 条件：时间段有交集 且 实例开始时间不早于 schedule 创建日期
     */
    private boolean isInstanceInQueryRange(LocalDateTime instanceStart, LocalDateTime instanceEnd,
                                           LocalDateTime queryStart, LocalDateTime queryEnd,
                                           LocalDateTime scheduleCreatedAt) {
        boolean hasTimeOverlap = instanceEnd.isAfter(queryStart) && instanceStart.isBefore(queryEnd);
        boolean notBeforeCreation = !instanceStart.isBefore(scheduleCreatedAt.toLocalDate().atStartOfDay());
        return hasTimeOverlap && notBeforeCreation;
    }

    private List<DayOfWeek> parseRepeatRuleDays(JSONObject repeatRuleConfig) {
        List<String> rawDays = repeatRuleConfig.getList("daysOfWeek", String.class);
        if (CollUtil.isEmpty(rawDays)) {
            return Collections.emptyList();
        }

        List<DayOfWeek> ruleDays = Lists.newArrayList();
        for (String rawDay : rawDays) {
            if (StrUtil.isBlank(rawDay)) {
                continue;
            }
            try {
                ruleDays.add(DayOfWeek.valueOf(rawDay));
            } catch (Exception error) {
                log.warn("Invalid repeat rule day: {}", rawDay);
            }
        }
        return ruleDays;
    }

    private String buildRepeatRuleConfig(List<String> daysOfWeek) {
        JSONObject config = new JSONObject();
        config.put("daysOfWeek", daysOfWeek);
        return JSONObject.toJSONString(config);
    }

    private void validateCustomRepeatRuleConfig(String repeatRuleConfig) {
        if (StrUtil.isBlank(repeatRuleConfig)) {
            throw new ApiException(ScheduleExceptionEnum.REPEAT_RULE_CONFIG_INVALID);
        }
        JSONObject config = JSONObject.parse(repeatRuleConfig);
        if (config == null) {
            throw new ApiException(ScheduleExceptionEnum.REPEAT_RULE_CONFIG_INVALID);
        }
        List<String> rawDays = config.getList("daysOfWeek", String.class);
        if (CollUtil.isEmpty(rawDays)) {
            throw new ApiException(ScheduleExceptionEnum.REPEAT_RULE_CONFIG_INVALID);
        }
        for (String rawDay : rawDays) {
            if (StrUtil.isBlank(rawDay)) {
                throw new ApiException(ScheduleExceptionEnum.REPEAT_RULE_CONFIG_INVALID);
            }

            try {
                DayOfWeek.valueOf(rawDay);
            } catch (Exception error) {
                throw new ApiException(ScheduleExceptionEnum.REPEAT_RULE_CONFIG_INVALID);
            }
        }
    }

    @Override
    public ScheduleDTO upsert(ScheduleReq scheduleReq) {
        Long currentUserId = ContextUtils.currentUser().getId();
        log.info("Upsert schedule, userId: {}, req: {}", currentUserId, scheduleReq);

        RepeatRuleEnum repeatRuleType = scheduleReq.getRepeatRuleType();
        if (RepeatRuleEnum.DAILY == repeatRuleType) {
            scheduleReq.setRepeatRuleConfig(buildRepeatRuleConfig(DAILY_RULE_DAYS));
        } else if (RepeatRuleEnum.WEEKDAY == repeatRuleType) {
            scheduleReq.setRepeatRuleConfig(buildRepeatRuleConfig(WEEKDAY_RULE_DAYS));
        } else if (RepeatRuleEnum.CUSTOM == repeatRuleType) {
            validateCustomRepeatRuleConfig(scheduleReq.getRepeatRuleConfig());
        }

        if (scheduleReq.getId() != null) {
            Schedule existingSchedule = scheduleRepository.lambdaQuery()
                    .eq(Schedule::getId, scheduleReq.getId())
                    .eq(Schedule::getUserId, currentUserId)
                    .one();
            if (Objects.isNull(existingSchedule)) {
                log.warn("Schedule not found: {}", scheduleReq.getId());
                throw new ApiException(ScheduleExceptionEnum.SCHEDULE_NOT_FOUND);
            }
        }
        Schedule schedule = convertToEntity(scheduleReq, currentUserId);
        scheduleRepository.saveOrUpdate(schedule);
        return convertToDTO(schedule);
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
                    .startDateTime(startDateTime)
                    .endDateTime(endDateTime)
                    .userId(currentUserId)
                    .build();

            pendingActivities.add(activity);
        }

        if (CollUtil.isEmpty(pendingActivities)) {
            return;
        }

        Set<LocalDateTime> startTimes = pendingActivities.stream()
                .map(Activity::getStartDateTime)
                .collect(Collectors.toSet());

        List<Activity> existingActivities = activityRepository.lambdaQuery()
                .in(Activity::getScheduleId, scheduleIds)
                .in(Activity::getStartDateTime, startTimes)
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
                .eq(Activity::getStartDateTime, req.getStartDateTime())
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
                .startTime(scheduleReq.getStartDateTime().toLocalTime())
                .endTime(scheduleReq.getEndDateTime().toLocalTime())
                .startDateTime(scheduleReq.getStartDateTime())
                .endDateTime(scheduleReq.getEndDateTime())
                .repeatRuleType(scheduleReq.getRepeatRuleType())
                .repeatRuleConfig(scheduleReq.getRepeatRuleConfig())
                .groupId(scheduleReq.getGroupId())
                .userId(userId)
                .build();
    }

    private String buildActivityKey(Activity activity) {
        return buildActivityKey(activity.getScheduleId(), activity.getStartDateTime());
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
        dto.setRepeatRuleType(schedule.getRepeatRuleType());
        dto.setRepeatRuleConfig(schedule.getRepeatRuleConfig());
        dto.setGroupId(schedule.getGroupId());
        dto.setUserId(schedule.getUserId());
        dto.setCreatedAt(schedule.getCreatedAt());
        return dto;
    }
}

package com.adhd.server.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.adhd.server.common.enums.TypeEnum;
import com.adhd.server.common.util.ContextUtils;
import com.adhd.server.dao.entity.Activity;
import com.adhd.server.dao.entity.Goal;
import com.adhd.server.dao.repository.ActivityRepository;
import com.adhd.server.dao.repository.GoalRepository;
import com.adhd.server.pojo.req.DashboardStatsReq;
import com.adhd.server.pojo.req.ListScheduleReq;
import com.adhd.server.pojo.resp.*;
import com.adhd.server.service.DashboardService;
import com.adhd.server.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author wangsiqian
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private static final int NEXT_RANGE_DAYS = 30;

    private final ActivityRepository activityRepository;
    private final GoalRepository goalRepository;
    private final ScheduleService scheduleService;

    @Override
    public DashboardFocusResp focus() {
        Long currentUserId = ContextUtils.currentUser().getId();
        LocalDateTime now = LocalDateTime.now();
        Activity currentActivity = findCurrentActivity(currentUserId, now);
        FocusTaskResp current = ObjectUtil.isNotNull(currentActivity)
                ? buildFocusTask(currentActivity)
                : buildCurrentScheduleFocus(now);
        FocusTaskResp next = buildNextFocus(currentUserId, now, ObjectUtil.isNotNull(currentActivity));
        return DashboardFocusResp.builder()
                .current(current)
                .next(next)
                .build();
    }

    private Activity findCurrentActivity(Long currentUserId, LocalDateTime now) {
        return activityRepository.lambdaQuery()
                .eq(Activity::getUserId, currentUserId)
                .le(Activity::getStartDateTime, now)
                .ge(Activity::getEndDateTime, now)
                .orderByDesc(Activity::getStartDateTime)
                .last("limit 1")
                .one();
    }

    private FocusTaskResp buildCurrentScheduleFocus(LocalDateTime now) {
        Optional<ScheduleDTO> currentSchedule = listSchedules(now, now).stream()
                .filter(schedule -> !schedule.getStartDateTime().isAfter(now)
                        && schedule.getEndDateTime().isAfter(now))
                .max(Comparator.comparing(ScheduleDTO::getStartDateTime));
        return currentSchedule.map(this::buildFocusTask).orElse(null);
    }

    private FocusTaskResp buildNextFocus(Long currentUserId, LocalDateTime now, boolean preferResumingSchedule) {
        if (preferResumingSchedule) {
            Optional<ScheduleDTO> resumingSchedule = listSchedules(now, now).stream()
                    .filter(schedule -> Boolean.TRUE.equals(schedule.getIsResuming()))
                    .findFirst();
            if (resumingSchedule.isPresent()) {
                return buildFocusTask(resumingSchedule.get());
            }
        }

        Activity nextActivity = activityRepository.lambdaQuery()
                .eq(Activity::getUserId, currentUserId)
                .gt(Activity::getStartDateTime, now)
                .orderByAsc(Activity::getStartDateTime)
                .last("limit 1")
                .one();
        if (ObjectUtil.isNotNull(nextActivity)) {
            return buildFocusTask(nextActivity);
        }

        LocalDateTime rangeEnd = now.plusDays(NEXT_RANGE_DAYS);
        Optional<ScheduleDTO> nextSchedule = listSchedules(now, rangeEnd).stream()
                .filter(schedule -> schedule.getStartDateTime().isAfter(now))
                .min(Comparator.comparing(ScheduleDTO::getStartDateTime));
        return nextSchedule.map(this::buildFocusTask).orElse(null);
    }

    private List<ScheduleDTO> listSchedules(LocalDateTime start, LocalDateTime end) {
        return scheduleService.list(ListScheduleReq.builder()
                .startDateTime(start)
                .endDateTime(end)
                .fullDetail(Boolean.TRUE)
                .build());
    }

    private FocusTaskResp buildFocusTask(Activity activity) {
        return FocusTaskResp.builder()
                .id(activity.getId())
                .scheduleId(activity.getScheduleId())
                .title(activity.getTitle())
                .content(activity.getContent())
                .type(activity.getType())
                .zone(activity.getZone())
                .goalId(activity.getGoalId())
                .startDateTime(activity.getStartDateTime())
                .endDateTime(activity.getEndDateTime())
                .isResuming(Boolean.FALSE)
                .build();
    }

    private FocusTaskResp buildFocusTask(ScheduleDTO schedule) {
        return FocusTaskResp.builder()
                .id(schedule.getId())
                .scheduleId(schedule.getId())
                .title(schedule.getTitle())
                .content(schedule.getContent())
                .type(schedule.getType())
                .zone(schedule.getZone())
                .goalId(schedule.getGoalId())
                .startDateTime(schedule.getStartDateTime())
                .endDateTime(schedule.getEndDateTime())
                .isResuming(schedule.getIsResuming())
                .build();
    }

    @Override
    public List<DistributionItemResp> distribution(DashboardStatsReq req) {
        Long currentUserId = ContextUtils.currentUser().getId();
        LocalDateTime startDateTime = req.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = req.getEndDate().atTime(LocalTime.MAX);

        List<Activity> activities = activityRepository.lambdaQuery()
                .eq(Activity::getUserId, currentUserId)
                .le(Activity::getStartDateTime, endDateTime)
                .ge(Activity::getEndDateTime, startDateTime)
                .list();

        // 计算总时间范围（分钟）
        long totalRangeMinutes = Duration.between(startDateTime, endDateTime).toMinutes();

        // 按 type 分组计算时长（排除 OTHER）
        Map<TypeEnum, Long> typeMinutesMap = new EnumMap<>(TypeEnum.class);

        long trackedMinutes = 0L;
        for (Activity activity : activities) {
            // 计算在查询范围内的有效时长
            LocalDateTime effectiveStart = activity.getStartDateTime().isBefore(startDateTime)
                    ? startDateTime : activity.getStartDateTime();
            LocalDateTime effectiveEnd = activity.getEndDateTime().isAfter(endDateTime)
                    ? endDateTime : activity.getEndDateTime();
            long minutes = Duration.between(effectiveStart, effectiveEnd).toMinutes();

            if (minutes > 0 && activity.getType() != null && activity.getType() != TypeEnum.OTHER) {
                typeMinutesMap.merge(activity.getType(), minutes, Long::sum);
                trackedMinutes += minutes;
            }
        }

        // 构建结果
        List<DistributionItemResp> result = new ArrayList<>();
        for (Map.Entry<TypeEnum, Long> entry : typeMinutesMap.entrySet()) {
            if (entry.getValue() > 0) {
                result.add(DistributionItemResp.builder()
                        .type(entry.getKey())
                        .value(entry.getValue())
                        .build());
            }
        }

        // 计算"其它"时间
        long otherMinutes = totalRangeMinutes - trackedMinutes;
        if (otherMinutes > 0) {
            result.add(DistributionItemResp.builder()
                    .type(TypeEnum.OTHER)
                    .value(otherMinutes)
                    .build());
        }

        return result;
    }

    @Override
    public AlignmentResp alignment(DashboardStatsReq req) {
        Long currentUserId = ContextUtils.currentUser().getId();
        LocalDateTime startDateTime = req.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = req.getEndDate().atTime(LocalTime.MAX);

        // 获取时间范围内的所有计划
        List<ScheduleDTO> schedules = listSchedules(startDateTime, endDateTime);
        int totalCount = schedules.size();

        if (totalCount == 0) {
            return AlignmentResp.builder()
                    .completedCount(0)
                    .totalCount(0)
                    .completionRate(BigDecimal.ZERO)
                    .actualMinutes(0L)
                    .plannedMinutes(0L)
                    .timeCompletionRate(BigDecimal.ZERO)
                    .goalAlignments(Collections.emptyList())
                    .build();
        }

        // 获取时间范围内的所有实际事项
        List<Activity> activities = activityRepository.lambdaQuery()
                .eq(Activity::getUserId, currentUserId)
                .le(Activity::getStartDateTime, endDateTime)
                .ge(Activity::getEndDateTime, startDateTime)
                .isNotNull(Activity::getScheduleId)
                .ne(Activity::getScheduleId, 0L)
                .list();

        // 统计已完成的计划数
        int completedCount = (int) schedules.stream()
                .filter(schedule -> Boolean.TRUE.equals(schedule.getCompleted()))
                .count();

        // 计算计划总时长
        long plannedMinutes = schedules.stream()
                .mapToLong(s -> calculateMinutesInRange(s.getStartDateTime(), s.getEndDateTime(), startDateTime, endDateTime))
                .sum();

        // 计算实际总时长
        long actualMinutes = activities.stream()
                .mapToLong(a -> calculateMinutesInRange(a.getStartDateTime(), a.getEndDateTime(), startDateTime, endDateTime))
                .sum();

        BigDecimal completionRate = BigDecimal.valueOf(completedCount)
                .divide(BigDecimal.valueOf(totalCount), 4, RoundingMode.HALF_UP);

        BigDecimal timeCompletionRate = plannedMinutes > 0
                ? BigDecimal.valueOf(actualMinutes).divide(BigDecimal.valueOf(plannedMinutes), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 按目标分组计算
        List<GoalAlignmentResp> goalAlignments = calculateGoalAlignments(schedules, activities, startDateTime, endDateTime);

        return AlignmentResp.builder()
                .completedCount(completedCount)
                .totalCount(totalCount)
                .completionRate(completionRate)
                .actualMinutes(actualMinutes)
                .plannedMinutes(plannedMinutes)
                .timeCompletionRate(timeCompletionRate)
                .goalAlignments(goalAlignments)
                .build();
    }

    private long calculateMinutesInRange(LocalDateTime start, LocalDateTime end,
                                         LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        LocalDateTime effectiveStart = start.isBefore(rangeStart) ? rangeStart : start;
        LocalDateTime effectiveEnd = end.isAfter(rangeEnd) ? rangeEnd : end;
        long minutes = Duration.between(effectiveStart, effectiveEnd).toMinutes();
        return Math.max(0, minutes);
    }

    private List<GoalAlignmentResp> calculateGoalAlignments(List<ScheduleDTO> schedules, List<Activity> activities,
                                                            LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // 收集所有涉及的 goalId
        Set<Long> goalIds = new HashSet<>();
        schedules.forEach(s -> {
            if (s.getGoalId() != null) goalIds.add(s.getGoalId());
        });
        activities.forEach(a -> {
            if (a.getGoalId() != null) goalIds.add(a.getGoalId());
        });

        if (goalIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 查询目标信息
        Map<Long, Goal> goalMap = goalRepository.lambdaQuery()
                .in(Goal::getId, goalIds)
                .list()
                .stream()
                .collect(Collectors.toMap(Goal::getId, Function.identity()));

        // 按目标分组计划
        Map<Long, List<ScheduleDTO>> schedulesByGoal = schedules.stream()
                .filter(s -> s.getGoalId() != null)
                .collect(Collectors.groupingBy(ScheduleDTO::getGoalId));

        // 按目标分组实际事项
        Map<Long, List<Activity>> activitiesByGoal = activities.stream()
                .filter(a -> a.getGoalId() != null)
                .collect(Collectors.groupingBy(Activity::getGoalId));

        // 计算每个目标的对齐度
        List<GoalAlignmentResp> result = new ArrayList<>();
        for (Long goalId : goalIds) {
            Goal goal = goalMap.get(goalId);
            List<ScheduleDTO> goalSchedules = schedulesByGoal.getOrDefault(goalId, Collections.emptyList());
            List<Activity> goalActivities = activitiesByGoal.getOrDefault(goalId, Collections.emptyList());

            int total = goalSchedules.size();
            int completed = (int) goalSchedules.stream()
                    .filter(s -> Boolean.TRUE.equals(s.getCompleted()))
                    .count();

            long planned = goalSchedules.stream()
                    .mapToLong(s -> calculateMinutesInRange(s.getStartDateTime(), s.getEndDateTime(), startDateTime, endDateTime))
                    .sum();

            long actual = goalActivities.stream()
                    .mapToLong(a -> calculateMinutesInRange(a.getStartDateTime(), a.getEndDateTime(), startDateTime, endDateTime))
                    .sum();

            BigDecimal compRate = total > 0
                    ? BigDecimal.valueOf(completed).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            BigDecimal timeRate = planned > 0
                    ? BigDecimal.valueOf(actual).divide(BigDecimal.valueOf(planned), 4, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            result.add(GoalAlignmentResp.builder()
                    .goalId(goalId)
                    .goalTitle(goal != null ? goal.getTitle() : null)
                    .completedCount(completed)
                    .totalCount(total)
                    .completionRate(compRate)
                    .actualMinutes(actual)
                    .plannedMinutes(planned)
                    .timeCompletionRate(timeRate)
                    .build());
        }

        return result;
    }

    @Override
    public List<DistributionTrendItemResp> distributionTrend(DashboardStatsReq req) {
        Long currentUserId = ContextUtils.currentUser().getId();
        LocalDateTime startDateTime = req.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = req.getEndDate().atTime(LocalTime.MAX);

        List<Activity> activities = activityRepository.lambdaQuery()
                .eq(Activity::getUserId, currentUserId)
                .le(Activity::getStartDateTime, endDateTime)
                .ge(Activity::getEndDateTime, startDateTime)
                .list();

        // 按天分组活动
        Map<LocalDate, List<Activity>> activityByDate = new HashMap<>();
        for (Activity activity : activities) {
            LocalDate date = activity.getStartDateTime().toLocalDate();
            activityByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(activity);
        }

        // 遍历每一天生成趋势数据
        List<DistributionTrendItemResp> result = new ArrayList<>();
        LocalDate current = req.getStartDate();
        while (!current.isAfter(req.getEndDate())) {
            List<Activity> dayActivities = activityByDate.getOrDefault(current, Collections.emptyList());
            LocalDateTime dayStart = current.atStartOfDay();
            LocalDateTime dayEnd = current.atTime(LocalTime.MAX);

            // 按类型统计当天时长
            Map<TypeEnum, Long> typeMinutes = new EnumMap<>(TypeEnum.class);
            long trackedMinutes = 0L;

            for (Activity activity : dayActivities) {
                long minutes = calculateMinutesInRange(activity.getStartDateTime(), activity.getEndDateTime(), dayStart, dayEnd);
                if (minutes > 0 && activity.getType() != null && activity.getType() != TypeEnum.OTHER) {
                    typeMinutes.merge(activity.getType(), minutes, Long::sum);
                    trackedMinutes += minutes;
                }
            }

            // 计算当天其它时间（24小时 - 已记录时间）
            long dayTotalMinutes = 24 * 60;
            long otherMinutes = dayTotalMinutes - trackedMinutes;

            result.add(DistributionTrendItemResp.builder()
                    .date(current)
                    .work(typeMinutes.getOrDefault(TypeEnum.WORK, 0L))
                    .invest(typeMinutes.getOrDefault(TypeEnum.INVEST, 0L))
                    .study(typeMinutes.getOrDefault(TypeEnum.STUDY, 0L))
                    .life(typeMinutes.getOrDefault(TypeEnum.LIFE, 0L))
                    .other(otherMinutes)
                    .build());

            current = current.plusDays(1);
        }

        return result;
    }

    @Override
    public List<GoalTrendItemResp> goalTrend(DashboardStatsReq req) {
        Long currentUserId = ContextUtils.currentUser().getId();
        LocalDateTime startDateTime = req.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = req.getEndDate().atTime(LocalTime.MAX);

        List<Activity> activities = activityRepository.lambdaQuery()
                .eq(Activity::getUserId, currentUserId)
                .le(Activity::getStartDateTime, endDateTime)
                .ge(Activity::getEndDateTime, startDateTime)
                .list();

        List<ScheduleDTO> schedules = listSchedules(startDateTime, endDateTime);

        // 收集所有涉及的 goalId
        Set<Long> goalIds = new HashSet<>();
        schedules.forEach(s -> {
            if (s.getGoalId() != null) goalIds.add(s.getGoalId());
        });

        if (goalIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 查询目标信息
        Map<Long, Goal> goalMap = goalRepository.lambdaQuery()
                .in(Goal::getId, goalIds)
                .list()
                .stream()
                .collect(Collectors.toMap(Goal::getId, Function.identity()));

        // 遍历每一天、每个目标生成趋势数据
        List<GoalTrendItemResp> result = new ArrayList<>();
        LocalDate current = req.getStartDate();
        while (!current.isAfter(req.getEndDate())) {
            LocalDateTime dayStart = current.atStartOfDay();
            LocalDateTime dayEnd = current.atTime(LocalTime.MAX);
            LocalDate finalCurrent = current;

            for (Long goalId : goalIds) {
                Goal goal = goalMap.get(goalId);

                // 当天该目标的计划
                List<ScheduleDTO> dayGoalSchedules = schedules.stream()
                        .filter(s -> goalId.equals(s.getGoalId()))
                        .filter(s -> s.getStartDateTime().toLocalDate().equals(finalCurrent))
                        .collect(Collectors.toList());

                // 当天该目标的实际事项
                List<Activity> dayGoalActivities = activities.stream()
                        .filter(a -> goalId.equals(a.getGoalId()))
                        .filter(a -> a.getStartDateTime().toLocalDate().equals(finalCurrent))
                        .collect(Collectors.toList());

                // 计算完成率
                BigDecimal completionRate = BigDecimal.ZERO;
                if (CollUtil.isNotEmpty(dayGoalSchedules)) {
                    long completedCount = dayGoalSchedules.stream()
                            .filter(s -> Boolean.TRUE.equals(s.getCompleted()))
                            .count();
                    completionRate = BigDecimal.valueOf(completedCount)
                            .divide(BigDecimal.valueOf(dayGoalSchedules.size()), 4, RoundingMode.HALF_UP);
                }

                // 计算实际时长
                long actualMinutes = dayGoalActivities.stream()
                        .mapToLong(a -> calculateMinutesInRange(a.getStartDateTime(), a.getEndDateTime(), dayStart, dayEnd))
                        .sum();

                result.add(GoalTrendItemResp.builder()
                        .date(current)
                        .goalId(goalId)
                        .goalTitle(goal != null ? goal.getTitle() : null)
                        .completionRate(completionRate)
                        .actualMinutes(actualMinutes)
                        .build());
            }

            current = current.plusDays(1);
        }

        return result;
    }
}

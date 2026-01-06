package com.pdhd.server.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.pdhd.server.common.util.ContextUtils;
import com.pdhd.server.dao.entity.Activity;
import com.pdhd.server.dao.repository.ActivityRepository;
import com.pdhd.server.pojo.req.ListScheduleReq;
import com.pdhd.server.pojo.resp.DashboardFocusResp;
import com.pdhd.server.pojo.resp.FocusTaskResp;
import com.pdhd.server.pojo.resp.ScheduleDTO;
import com.pdhd.server.service.DashboardService;
import com.pdhd.server.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * @author wangsiqian
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private static final int NEXT_RANGE_DAYS = 30;

    private final ActivityRepository activityRepository;
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
}

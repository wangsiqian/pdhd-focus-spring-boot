package com.adhd.server.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.adhd.server.common.enums.RepeatRuleEnum;
import com.adhd.server.common.exception.ApiException;
import com.adhd.server.common.util.ContextUtils;
import com.adhd.server.dao.entity.Activity;
import com.adhd.server.dao.entity.Goal;
import com.adhd.server.dao.entity.Schedule;
import com.adhd.server.dao.repository.ActivityRepository;
import com.adhd.server.dao.repository.GoalRepository;
import com.adhd.server.dao.repository.ScheduleRepository;
import com.adhd.server.exception.GoalExceptionEnum;
import com.adhd.server.pojo.req.GoalReq;
import com.adhd.server.pojo.req.ListGoalReq;
import com.adhd.server.pojo.resp.GoalDTO;
import com.adhd.server.pojo.resp.ScheduleDTO;
import com.adhd.server.service.GoalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author adhd
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {
    private final GoalRepository goalRepository;
    private final ScheduleRepository scheduleRepository;
    private final ActivityRepository activityRepository;

    @Override
    public GoalDTO getById(Long id) {
        Goal goal = goalRepository.getById(id);
        if (Objects.isNull(goal)) {
            log.info("目标不存在：{}", id);
            throw new ApiException(GoalExceptionEnum.GOAL_NOT_FOUND);
        }

        // 检查目标是否属于当前用户
        Long currentUserId = ContextUtils.currentUser().getId();
        if (!Objects.equals(goal.getUserId(), currentUserId)) {
            log.info("用户无权限访问此目标：{}", id);
            throw new ApiException(GoalExceptionEnum.GOAL_NOT_FOUND);
        }

        return convertToDTO(goal);
    }

    @Override
    public List<GoalDTO> list(ListGoalReq req) {
        Long currentUserId = ContextUtils.currentUser().getId();

        // 构建查询条件
        List<Goal> goals = goalRepository.lambdaQuery()
                .eq(Goal::getUserId, currentUserId)
                .orderByAsc(Goal::getCreatedAt)
                .list();

        if (CollUtil.isEmpty(goals)) {
            return Lists.newArrayList();
        }

        Set<Long> goalIds = goals.stream()
                .map(Goal::getId)
                .collect(Collectors.toSet());
        boolean fullDetail = BooleanUtil.isTrue(req.getFullDetail());
        Map<Long, List<ScheduleDTO>> repeatScheduleMap = fullDetail ? buildRepeatScheduleMap(currentUserId, goalIds) : Maps.newHashMap();
        Map<Long, LocalDateTime> latestActivityUpdateMap = fullDetail ? buildLatestActivityUpdateMap(currentUserId, goalIds) : Maps.newHashMap();

        // 根据fullDetail参数决定是否返回content字段（对Goal而言，目前所有字段都返回）
        return goals.stream()
                .map(goal -> {
                    GoalDTO dto = convertToDTO(goal);
                    if (fullDetail) {
                        dto.setRepeatSchedules(repeatScheduleMap.getOrDefault(goal.getId(), Lists.newArrayList()));
                        dto.setUpdatedAt(latestActivityUpdateMap.getOrDefault(goal.getId(), goal.getUpdatedAt()));
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public GoalDTO upsert(GoalReq goalReq) {
        Long currentUserId = ContextUtils.currentUser().getId();
        if (goalReq.getId() != null) {
            Goal existingGoal = goalRepository.lambdaQuery()
                    .eq(Goal::getId, goalReq.getId())
                    .eq(Goal::getUserId, currentUserId)
                    .one();
            if (Objects.isNull(existingGoal)) {
                throw new ApiException(GoalExceptionEnum.GOAL_NOT_FOUND);
            }
        }

        Goal goal = convertToEntity(goalReq, currentUserId);
        goalRepository.saveOrUpdate(goal);
        return convertToDTO(goal);
    }

    @Override
    public void delete(Long id) {
        Goal existingGoal = goalRepository.getById(id);
        if (Objects.isNull(existingGoal)) {
            log.info("目标不存在：{}", id);
            throw new ApiException(GoalExceptionEnum.GOAL_NOT_FOUND);
        }

        // 检查目标是否属于当前用户
        Long currentUserId = ContextUtils.currentUser().getId();
        if (!Objects.equals(existingGoal.getUserId(), currentUserId)) {
            log.info("用户无权限删除此目标：{}", id);
            throw new ApiException(GoalExceptionEnum.GOAL_NOT_FOUND);
        }

        goalRepository.removeById(id);
    }

    private Goal convertToEntity(GoalReq goalReq, Long userId) {
        return Goal.builder()
                .id(goalReq.getId())
                .title(goalReq.getTitle())
                .color(goalReq.getColor())
                .status(goalReq.getStatus())
                .progress(goalReq.getProgress())
                .userId(userId)
                .build();
    }

    private GoalDTO convertToDTO(Goal goal) {
        GoalDTO dto = new GoalDTO();
        dto.setId(goal.getId());
        dto.setTitle(goal.getTitle());
        dto.setColor(goal.getColor());
        dto.setStatus(goal.getStatus());
        dto.setProgress(goal.getProgress());
        dto.setUserId(goal.getUserId());
        dto.setCreatedAt(goal.getCreatedAt());
        dto.setUpdatedAt(goal.getUpdatedAt());
        return dto;
    }

    private Map<Long, List<ScheduleDTO>> buildRepeatScheduleMap(Long currentUserId, Set<Long> goalIds) {
        List<Schedule> schedules = scheduleRepository.lambdaQuery()
                .eq(Schedule::getUserId, currentUserId)
                .in(Schedule::getGoalId, goalIds)
                .ne(Schedule::getRepeatRuleType, RepeatRuleEnum.NONE)
                .list();
        Map<Long, List<ScheduleDTO>> scheduleMap = Maps.newHashMap();
        if (CollUtil.isEmpty(schedules)) {
            return scheduleMap;
        }
        for (Schedule schedule : schedules) {
            List<ScheduleDTO> list = scheduleMap.computeIfAbsent(schedule.getGoalId(), key -> Lists.newArrayList());
            list.add(convertToScheduleDTO(schedule));
        }
        return scheduleMap;
    }

    private Map<Long, LocalDateTime> buildLatestActivityUpdateMap(Long currentUserId, Set<Long> goalIds) {
        List<Activity> activities = activityRepository.list(Wrappers.<Activity>query()
                .select("goal_id", "max(updated_at) as updated_at")
                .lambda()
                .eq(Activity::getUserId, currentUserId)
                .in(Activity::getGoalId, goalIds)
                .groupBy(Activity::getGoalId));
        Map<Long, LocalDateTime> result = Maps.newHashMap();
        if (CollUtil.isEmpty(activities)) {
            return result;
        }
        for (Activity activity : activities) {
            result.put(activity.getGoalId(), activity.getUpdatedAt());
        }
        return result;
    }

    private ScheduleDTO convertToScheduleDTO(Schedule schedule) {
        ScheduleDTO dto = new ScheduleDTO();
        dto.setId(schedule.getId());
        dto.setTitle(schedule.getTitle());
        dto.setContent(schedule.getContent());
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

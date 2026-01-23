package com.adhd.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.adhd.server.common.exception.ApiException;
import com.adhd.server.common.util.ContextUtils;
import com.adhd.server.dao.entity.Activity;
import com.adhd.server.dao.repository.ActivityRepository;
import com.adhd.server.exception.ActivityExceptionEnum;
import com.adhd.server.pojo.req.ActivityReq;
import com.adhd.server.pojo.req.ListActivityReq;
import com.adhd.server.pojo.req.ListPLanReq;
import com.adhd.server.pojo.resp.ActivityDTO;
import com.adhd.server.pojo.resp.ActivityPlanDTO;
import com.adhd.server.service.ActivityService;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author adhd
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {
    private final ActivityRepository activityRepository;

    @Override
    public ActivityDTO getById(Long id) {
        Activity activity = activityRepository.getById(id);
        if (Objects.isNull(activity)) {
            log.info("实际事项不存在：{}", id);
            throw new ApiException(ActivityExceptionEnum.ACTIVITY_NOT_FOUND);
        }

        // 检查实际事项是否属于当前用户
        Long currentUserId = ContextUtils.currentUser().getId();
        if (!Objects.equals(activity.getUserId(), currentUserId)) {
            log.info("用户无权限访问此实际事项：{}", id);
            throw new ApiException(ActivityExceptionEnum.ACTIVITY_NOT_FOUND);
        }

        return convertToDTO(activity);
    }

    @Override
    public List<ActivityDTO> list(ListActivityReq req) {
        Long currentUserId = ContextUtils.currentUser().getId();
        
        // 构建查询条件
        List<Activity> activities = activityRepository.lambdaQuery()
                .eq(Activity::getUserId, currentUserId)
                .ge(Objects.nonNull(req.getStartTime()), Activity::getStartDateTime, req.getStartTime())
                .le(Objects.nonNull(req.getEndTime()), Activity::getEndDateTime, req.getEndTime())
                .orderByAsc(Activity::getCreatedAt)
                .list();
        
        // 根据fullDetail参数决定是否返回content字段
        return activities.stream()
                .map(activity -> convertToDTO(activity, req.getFullDetail()))
                .collect(Collectors.toList());
    }

    @Override
    public ActivityDTO upsert(ActivityReq activityReq) {
        Long currentUserId = ContextUtils.currentUser().getId();
        if (activityReq.getId() != null) {
            Activity existingActivity = activityRepository.lambdaQuery()
                    .eq(Activity::getId, activityReq.getId())
                    .eq(Activity::getUserId, currentUserId)
                    .one();
            if (Objects.isNull(existingActivity)) {
                log.info("实际事项不存在：{}", activityReq.getId());
                throw new ApiException(ActivityExceptionEnum.ACTIVITY_NOT_FOUND);
            }
        }
        Activity activity = convertToEntity(activityReq, currentUserId);
        activityRepository.saveOrUpdate(activity);
        return convertToDTO(activity);
    }

    @Override
    public void delete(Long id) {
        Activity existingActivity = activityRepository.getById(id);
        if (Objects.isNull(existingActivity)) {
            log.info("实际事项不存在：{}", id);
            throw new ApiException(ActivityExceptionEnum.ACTIVITY_NOT_FOUND);
        }

        // 检查实际事项是否属于当前用户
        Long currentUserId = ContextUtils.currentUser().getId();
        if (!Objects.equals(existingActivity.getUserId(), currentUserId)) {
            log.info("用户无权限删除此实际事项：{}", id);
            throw new ApiException(ActivityExceptionEnum.ACTIVITY_NOT_FOUND);
        }

        activityRepository.removeById(id);
    }

    private Activity convertToEntity(ActivityReq activityReq, Long userId) {
        return Activity.builder()
                .id(activityReq.getId())
                .scheduleId(activityReq.getScheduleId())
                .title(activityReq.getTitle())
                .content(activityReq.getContent())
                .type(activityReq.getType())
                .zone(activityReq.getZone())
                .goalId(activityReq.getGoalId())
                .startDateTime(activityReq.getStartDateTime())
                .endDateTime(activityReq.getEndDateTime())
                .userId(userId)
                .build();
    }

    private ActivityDTO convertToDTO(Activity activity) {
        return convertToDTO(activity, true); // 默认返回完整信息
    }
    
    private ActivityDTO convertToDTO(Activity activity, Boolean fullDetail) {
        ActivityDTO dto = new ActivityDTO();
        dto.setId(activity.getId());
        dto.setScheduleId(activity.getScheduleId());
        dto.setTitle(activity.getTitle());

        // 根据fullDetail参数决定是否返回content字段
        if (fullDetail != null && fullDetail) {
            dto.setContent(activity.getContent());
        }

        dto.setType(activity.getType());
        dto.setZone(activity.getZone());
        dto.setGoalId(activity.getGoalId());
        dto.setStartDateTime(activity.getStartDateTime());
        dto.setEndDateTime(activity.getEndDateTime());
        dto.setUserId(activity.getUserId());
        dto.setCreatedAt(activity.getCreatedAt());
        return dto;
    }

    @Override
    public List<ActivityPlanDTO> plan(ListPLanReq req) {
        log.debug("Get activity plan list, req: {}", req);
        Long currentUserId = ContextUtils.currentUser().getId();

        // 查询时间范围内的 activity 数据
        List<Activity> activities = activityRepository.lambdaQuery()
                .eq(Activity::getUserId, currentUserId)
                .le(Activity::getStartDateTime, req.getEndDateTime())
                .ge(Activity::getEndDateTime, req.getStartDateTime())
                .orderByAsc(Activity::getStartDateTime)
                .list();

        if (CollectionUtil.isEmpty(activities)) {
            return Collections.emptyList();
        }

        Map<LocalDate, List<ActivityDTO>> map = new HashMap<>();

        for (Activity activity : activities) {
            LocalDateTime startDateTime = activity.getStartDateTime();
            LocalDateTime endDateTime = activity.getEndDateTime();

            LocalDate startDate = startDateTime.toLocalDate();
            LocalDate endDate = endDateTime.toLocalDate();

            // 遍历该事件跨越的每一天
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

                ActivityDTO newDto = convertToDTO(activity);
                newDto.setStartDateTime(splitStart);
                newDto.setEndDateTime(splitEnd);

                map.computeIfAbsent(currentDate, k -> Lists.newArrayList()).add(newDto);
            }
        }

        // 转换为 ActivityPlanDTO 列表并排序
        return map.entrySet().stream()
                .map(entry -> {
                    ActivityPlanDTO planDTO = new ActivityPlanDTO();
                    planDTO.setDate(entry.getKey());
                    planDTO.setActivities(entry.getValue().stream()
                            .sorted(Comparator.comparing(ActivityDTO::getStartDateTime))
                            .collect(Collectors.toList()));
                    return planDTO;
                })
                .sorted(Comparator.comparing(ActivityPlanDTO::getDate))
                .collect(Collectors.toList());
    }
}

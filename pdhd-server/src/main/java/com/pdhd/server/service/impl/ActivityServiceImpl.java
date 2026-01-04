package com.pdhd.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pdhd.server.common.exception.ApiException;
import com.pdhd.server.common.util.ContextUtils;
import com.pdhd.server.dao.entity.Activity;
import com.pdhd.server.dao.repository.ActivityRepository;
import com.pdhd.server.exception.ActivityExceptionEnum;
import com.pdhd.server.pojo.resp.ActivityDTO;
import com.pdhd.server.req.ActivityReq;
import com.pdhd.server.req.ListActivityReq;
import com.pdhd.server.service.ActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author pdhd
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
                .ge(Objects.nonNull(req.getStartTime()), Activity::getStartTime, req.getStartTime())
                .le(Objects.nonNull(req.getEndTime()), Activity::getEndTime, req.getEndTime())
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
            // 更新操作
            Activity existingActivity = activityRepository.getById(activityReq.getId());
            if (Objects.isNull(existingActivity)) {
                log.info("实际事项不存在：{}", activityReq.getId());
                throw new ApiException(ActivityExceptionEnum.ACTIVITY_NOT_FOUND);
            }

            // 检查实际事项是否属于当前用户
            if (!Objects.equals(existingActivity.getUserId(), currentUserId)) {
                log.info("用户无权限修改此实际事项：{}", activityReq.getId());
                throw new ApiException(ActivityExceptionEnum.ACTIVITY_NOT_FOUND);
            }

            Activity activity = convertToEntity(activityReq, currentUserId);
            activity.setId(activityReq.getId());
            activity.setUserId(currentUserId); // 确保不会更改用户ID
            activityRepository.updateById(activity);
            return convertToDTO(activity);
        } else {
            // 创建操作
            Activity activity = convertToEntity(activityReq, currentUserId);
            activity.setUserId(currentUserId);
            activityRepository.save(activity);
            return convertToDTO(activity);
        }
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
                .startTime(activityReq.getStartTime())
                .endTime(activityReq.getEndTime())
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
        dto.setStartTime(activity.getStartTime());
        dto.setEndTime(activity.getEndTime());
        dto.setUserId(activity.getUserId());
        dto.setCreatedAt(activity.getCreatedAt());
        return dto;
    }
}
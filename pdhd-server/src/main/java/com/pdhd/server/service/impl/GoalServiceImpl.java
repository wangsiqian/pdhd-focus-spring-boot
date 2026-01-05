package com.pdhd.server.service.impl;

import com.pdhd.server.common.exception.ApiException;
import com.pdhd.server.common.util.ContextUtils;
import com.pdhd.server.dao.entity.Goal;
import com.pdhd.server.dao.repository.GoalRepository;
import com.pdhd.server.exception.GoalExceptionEnum;
import com.pdhd.server.pojo.req.GoalReq;
import com.pdhd.server.pojo.req.ListGoalReq;
import com.pdhd.server.pojo.resp.GoalDTO;
import com.pdhd.server.service.GoalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author pdhd
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {
    private final GoalRepository goalRepository;

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

        // 根据fullDetail参数决定是否返回content字段（对Goal而言，目前所有字段都返回）
        return goals.stream()
                .map(goal -> convertToDTO(goal, req.getFullDetail()))
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
        return convertToDTO(goal, true); // 默认返回完整信息
    }

    private GoalDTO convertToDTO(Goal goal, Boolean fullDetail) {
        GoalDTO dto = new GoalDTO();
        dto.setId(goal.getId());
        dto.setTitle(goal.getTitle());
        dto.setColor(goal.getColor());
        dto.setStatus(goal.getStatus());
        dto.setProgress(goal.getProgress());
        dto.setUserId(goal.getUserId());
        dto.setCreatedAt(goal.getCreatedAt());
        return dto;
    }
}
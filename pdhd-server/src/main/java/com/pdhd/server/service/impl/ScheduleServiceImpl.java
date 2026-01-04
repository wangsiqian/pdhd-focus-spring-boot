package com.pdhd.server.service.impl;

import com.pdhd.server.common.exception.ApiException;
import com.pdhd.server.common.util.ContextUtils;
import com.pdhd.server.dao.entity.Schedule;
import com.pdhd.server.dao.repository.ScheduleRepository;
import com.pdhd.server.exception.ScheduleExceptionEnum;
import com.pdhd.server.pojo.resp.ScheduleDTO;
import com.pdhd.server.req.ScheduleReq;
import com.pdhd.server.service.ScheduleService;
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
    public List<ScheduleDTO> list() {
        Long currentUserId = ContextUtils.currentUser().getId();
        List<Schedule> schedules = scheduleRepository.lambdaQuery()
                .eq(Schedule::getUserId, currentUserId)
                .list();
        return schedules.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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
                .status(scheduleReq.getStatus())
                .repeatRule(scheduleReq.getRepeatRule())
                .customDays(scheduleReq.getCustomDays())
                .groupId(scheduleReq.getGroupId())
                .userId(userId)
                .build();
    }

    private ScheduleDTO convertToDTO(Schedule schedule) {
        ScheduleDTO dto = new ScheduleDTO();
        dto.setId(schedule.getId());
        dto.setTitle(schedule.getTitle());
        dto.setContent(schedule.getContent());
        dto.setType(schedule.getType());
        dto.setZone(schedule.getZone());
        dto.setGoalId(schedule.getGoalId());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        dto.setStatus(schedule.getStatus());
        dto.setRepeatRule(schedule.getRepeatRule());
        dto.setCustomDays(schedule.getCustomDays());
        dto.setGroupId(schedule.getGroupId());
        dto.setUserId(schedule.getUserId());
        dto.setCreatedAt(schedule.getCreatedAt());
        return dto;
    }
}
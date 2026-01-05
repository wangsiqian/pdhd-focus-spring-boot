package com.pdhd.server.controller;

import com.pdhd.server.common.annotation.EnableApiResponse;
import com.pdhd.server.common.exception.ApiException;
import com.pdhd.server.common.util.ContextUtils;
import com.pdhd.server.exception.ScheduleExceptionEnum;
import com.pdhd.server.pojo.req.BatchCompleteScheduleReq;
import com.pdhd.server.pojo.req.CompleteScheduleReq;
import com.pdhd.server.pojo.req.GetByIdReq;
import com.pdhd.server.pojo.req.ListPLanReq;
import com.pdhd.server.pojo.req.ListScheduleReq;
import com.pdhd.server.pojo.req.ScheduleReq;
import com.pdhd.server.pojo.req.UncompleteScheduleReq;
import com.pdhd.server.pojo.resp.PlanDTO;
import com.pdhd.server.pojo.resp.ScheduleDTO;
import com.pdhd.server.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.util.List;

/**
 * @author pdhd
 */
@RequestMapping("/webApi/schedules")
@EnableApiResponse
@RequiredArgsConstructor
public class ScheduleController {
    private static final String SCHEDULE_COMPLETE_LOCK_KEY_PREFIX = "schedule:complete:";
    private final ScheduleService scheduleService;
    private final RedissonClient redissonClient;

    @PostMapping("/getById")
    public ScheduleDTO getById(@RequestBody GetByIdReq req) {
        return scheduleService.getById(req.getId());
    }

    @PostMapping("/list")
    public List<ScheduleDTO> list(@Valid @RequestBody ListScheduleReq req) {
        return scheduleService.list(req);
    }

    @PostMapping("/plan")
    public List<PlanDTO> plan(@Valid @RequestBody ListPLanReq req) {
        return scheduleService.plan(req);
    }

    @PostMapping("/upsert")
    public ScheduleDTO upsert(@Valid @RequestBody ScheduleReq scheduleReq) {
        return scheduleService.upsert(scheduleReq);
    }

    @PostMapping("/complete")
    public void complete(@RequestBody @Valid CompleteScheduleReq req) {
        Long currentUserId = ContextUtils.currentUser().getId();
        RLock lock = redissonClient.getLock(SCHEDULE_COMPLETE_LOCK_KEY_PREFIX + currentUserId);
        boolean locked = lock.tryLock();
        if (!locked) {
            throw new ApiException(ScheduleExceptionEnum.SCHEDULE_LOCK_BUSY);
        }
        try {
            scheduleService.complete(req);
        } finally {
            lock.unlock();
        }
    }

    @PostMapping("/complete/batch")
    public void batchComplete(@RequestBody @Valid BatchCompleteScheduleReq req) {
        Long currentUserId = ContextUtils.currentUser().getId();
        RLock lock = redissonClient.getLock(SCHEDULE_COMPLETE_LOCK_KEY_PREFIX + currentUserId);
        boolean locked = lock.tryLock();
        if (!locked) {
            throw new ApiException(ScheduleExceptionEnum.SCHEDULE_LOCK_BUSY);
        }
        try {
            scheduleService.batchComplete(req);
        } finally {
            lock.unlock();
        }
    }

    @PostMapping("/uncomplete")
    public void uncomplete(@RequestBody @Valid UncompleteScheduleReq req) {
        Long currentUserId = ContextUtils.currentUser().getId();
        RLock lock = redissonClient.getLock(SCHEDULE_COMPLETE_LOCK_KEY_PREFIX + currentUserId);
        boolean locked = lock.tryLock();
        if (!locked) {
            throw new ApiException(ScheduleExceptionEnum.SCHEDULE_LOCK_BUSY);
        }
        try {
            scheduleService.uncomplete(req);
        } finally {
            lock.unlock();
        }
    }

    @PostMapping("/delete")
    public void delete(@RequestBody GetByIdReq req) {
        scheduleService.delete(req.getId());
    }
}

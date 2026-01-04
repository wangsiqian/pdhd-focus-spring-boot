package com.pdhd.server.controller;

import com.pdhd.server.common.annotation.EnableApiResponse;
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
    private final ScheduleService scheduleService;

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
        scheduleService.complete(req);
    }

    @PostMapping("/complete/batch")
    public void batchComplete(@RequestBody @Valid BatchCompleteScheduleReq req) {
        scheduleService.batchComplete(req);
    }

    @PostMapping("/uncomplete")
    public void uncomplete(@RequestBody @Valid UncompleteScheduleReq req) {
        scheduleService.uncomplete(req);
    }

    @PostMapping("/delete")
    public void delete(@RequestBody GetByIdReq req) {
        scheduleService.delete(req.getId());
    }
}

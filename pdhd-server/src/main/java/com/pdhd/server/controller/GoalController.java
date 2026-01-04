package com.pdhd.server.controller;

import com.pdhd.server.common.annotation.EnableApiResponse;
import com.pdhd.server.pojo.resp.GoalDTO;
import com.pdhd.server.pojo.req.GetByIdReq;
import com.pdhd.server.pojo.req.GoalReq;
import com.pdhd.server.pojo.req.ListGoalReq;
import com.pdhd.server.service.GoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.util.List;

/**
 * @author pdhd
 */
@RequestMapping("/webApi/goals")
@EnableApiResponse
@RequiredArgsConstructor
public class GoalController {
    private final GoalService goalService;

    @PostMapping("/getById")
    public GoalDTO getById(@RequestBody GetByIdReq req) {
        return goalService.getById(req.getId());
    }

    @PostMapping("/list")
    public List<GoalDTO> list(@Valid @RequestBody ListGoalReq req) {
        return goalService.list(req);
    }

    @PostMapping("/upsert")
    public GoalDTO upsert(@Valid @RequestBody GoalReq goalReq) {
        return goalService.upsert(goalReq);
    }

    @PostMapping("/delete")
    public void delete(@RequestBody GetByIdReq req) {
        goalService.delete(req.getId());
    }
}
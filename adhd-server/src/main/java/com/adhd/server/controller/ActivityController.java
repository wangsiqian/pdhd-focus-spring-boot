package com.adhd.server.controller;

import com.adhd.server.common.annotation.EnableApiResponse;
import com.adhd.server.pojo.resp.ActivityDTO;
import com.adhd.server.pojo.req.ActivityReq;
import com.adhd.server.pojo.req.GetByIdReq;
import com.adhd.server.pojo.req.ListActivityReq;
import com.adhd.server.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.util.List;

/**
 * @author adhd
 */
@RequestMapping("/webApi/activities")
@EnableApiResponse
@RequiredArgsConstructor
public class ActivityController {
    private final ActivityService activityService;

    @PostMapping("/getById")
    public ActivityDTO getById(@RequestBody GetByIdReq req) {
        return activityService.getById(req.getId());
    }

    @PostMapping("/list")
    public List<ActivityDTO> list(@Valid @RequestBody ListActivityReq req) {
        return activityService.list(req);
    }

    @PostMapping("/upsert")
    public ActivityDTO upsert(@Valid @RequestBody ActivityReq activityReq) {
        return activityService.upsert(activityReq);
    }

    @PostMapping("/delete")
    public void delete(@RequestBody GetByIdReq req) {
        activityService.delete(req.getId());
    }
}
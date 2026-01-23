package com.adhd.server.controller;

import com.adhd.server.common.annotation.EnableApiResponse;
import com.adhd.server.pojo.req.DashboardStatsReq;
import com.adhd.server.pojo.resp.*;
import com.adhd.server.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.util.List;

/**
 * @author wangsiqian
 */
@RequestMapping("/webApi/dashboard")
@EnableApiResponse
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/focus")
    public DashboardFocusResp focus() {
        return dashboardService.focus();
    }

    @PostMapping("/distribution")
    public List<DistributionItemResp> distribution(@Valid @RequestBody DashboardStatsReq req) {
        return dashboardService.distribution(req);
    }

    @PostMapping("/alignment")
    public AlignmentResp alignment(@Valid @RequestBody DashboardStatsReq req) {
        return dashboardService.alignment(req);
    }

    @PostMapping("/distribution/trend")
    public List<DistributionTrendItemResp> distributionTrend(@Valid @RequestBody DashboardStatsReq req) {
        return dashboardService.distributionTrend(req);
    }

    @PostMapping("/goal/trend")
    public List<GoalTrendItemResp> goalTrend(@Valid @RequestBody DashboardStatsReq req) {
        return dashboardService.goalTrend(req);
    }
}

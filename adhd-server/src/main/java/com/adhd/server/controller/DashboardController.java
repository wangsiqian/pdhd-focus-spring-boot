package com.adhd.server.controller;

import com.adhd.server.common.annotation.EnableApiResponse;
import com.adhd.server.pojo.resp.DashboardFocusResp;
import com.adhd.server.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
}

package com.pdhd.server.controller;

import com.pdhd.server.common.annotation.EnableApiResponse;
import com.pdhd.server.pojo.resp.DashboardFocusResp;
import com.pdhd.server.service.DashboardService;
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

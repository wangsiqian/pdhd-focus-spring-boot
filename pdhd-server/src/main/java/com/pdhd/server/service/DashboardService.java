package com.pdhd.server.service;

import com.pdhd.server.pojo.resp.DashboardFocusResp;

/**
 * @author wangsiqian
 */
public interface DashboardService {
    /**
     * 获取当前与下一个专注任务
     *
     * @return 专注任务
     * @author wangsiqian
     */
    DashboardFocusResp focus();
}

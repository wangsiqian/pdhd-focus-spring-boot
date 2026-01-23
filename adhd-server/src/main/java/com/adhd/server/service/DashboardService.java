package com.adhd.server.service;

import com.adhd.server.pojo.req.DashboardStatsReq;
import com.adhd.server.pojo.resp.*;

import java.util.List;

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

    /**
     * 实际投入分布 - 按 type 分组统计时间
     *
     * @param req 时间范围
     * @return 分布统计列表
     */
    List<DistributionItemResp> distribution(DashboardStatsReq req);

    /**
     * 目标对齐度 - 计划完成度统计
     *
     * @param req 时间范围
     * @return 对齐度统计
     */
    AlignmentResp alignment(DashboardStatsReq req);

    /**
     * 时间分布趋势 - 按天统计各类型时间分布（柱状图）
     *
     * @param req 时间范围
     * @return 趋势统计列表
     */
    List<DistributionTrendItemResp> distributionTrend(DashboardStatsReq req);

    /**
     * 目标完成度趋势 - 按天按目标统计完成度（折线图）
     *
     * @param req 时间范围
     * @return 趋势统计列表
     */
    List<GoalTrendItemResp> goalTrend(DashboardStatsReq req);
}

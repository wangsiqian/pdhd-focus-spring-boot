package com.adhd.server.pojo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 目标对齐度响应
 *
 * @author wangsiqian
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlignmentResp {
    /**
     * 已完成计划数
     */
    private Integer completedCount;
    /**
     * 总计划数
     */
    private Integer totalCount;
    /**
     * 计划完成率 (0-1)
     */
    private BigDecimal completionRate;
    /**
     * 实际投入时长（分钟）
     */
    private Long actualMinutes;
    /**
     * 计划时长（分钟）
     */
    private Long plannedMinutes;
    /**
     * 时间完成率 (0-1)
     */
    private BigDecimal timeCompletionRate;
    /**
     * 按目标分组的对齐度
     */
    private List<GoalAlignmentResp> goalAlignments;
}

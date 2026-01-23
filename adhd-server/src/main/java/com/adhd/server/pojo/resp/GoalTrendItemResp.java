package com.adhd.server.pojo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 目标趋势项
 *
 * @author wangsiqian
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalTrendItemResp {
    /**
     * 日期
     */
    private LocalDate date;
    /**
     * 目标ID
     */
    private Long goalId;
    /**
     * 目标标题
     */
    private String goalTitle;
    /**
     * 当日完成率 (0-1)
     */
    private BigDecimal completionRate;
    /**
     * 当日实际投入时长（分钟）
     */
    private Long actualMinutes;
}

package com.adhd.server.pojo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 时间分布趋势项
 *
 * @author wangsiqian
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistributionTrendItemResp {
    /**
     * 日期
     */
    private LocalDate date;
    /**
     * 工作时长（分钟）
     */
    private Long work;
    /**
     * 投资时长（分钟）
     */
    private Long invest;
    /**
     * 学习时长（分钟）
     */
    private Long study;
    /**
     * 生活时长（分钟）
     */
    private Long life;
    /**
     * 其它时长（分钟）
     */
    private Long other;
}

package com.adhd.server.pojo.req;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * @author wangsiqian
 */
@Data
public class DashboardStatsReq {
    @NotNull
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;
}

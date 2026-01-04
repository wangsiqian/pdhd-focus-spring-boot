package com.pdhd.server.pojo.resp;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * @author wangsiqian
 */
@Data
public class PlanDTO {
    private LocalDate date;
    private List<ScheduleDTO> schedules;
}

package com.adhd.server.pojo.resp;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * @author wangsiqian
 */
@Data
public class ActivityPlanDTO {
    private LocalDate date;
    private List<ActivityDTO> activities;
}

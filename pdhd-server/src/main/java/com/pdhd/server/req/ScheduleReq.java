package com.pdhd.server.req;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author pdhd
 */
@Data
public class ScheduleReq {
    private Long id;
    
    @NotBlank(message = "计划标题不能为空")
    private String title;
    
    private String content;
    private String type;
    private String zone;
    private Long goalId;
    
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;
    
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;
    
    private Integer status;
    private String repeatRule;
    private String customDays;
    private String groupId;
}
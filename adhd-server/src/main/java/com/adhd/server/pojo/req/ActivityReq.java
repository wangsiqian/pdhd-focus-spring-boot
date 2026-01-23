package com.adhd.server.pojo.req;

import com.adhd.server.common.enums.TypeEnum;
import com.adhd.server.common.enums.ZoneTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author adhd
 */
@Data
public class ActivityReq {
    private Long id;
    
    private Long scheduleId;
    
    @NotBlank(message = "实际事项标题不能为空")
    private String title;
    
    private String content;
    private TypeEnum type;
    private ZoneTypeEnum zone;
    private Long goalId;
    
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startDateTime;
    
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endDateTime;
}
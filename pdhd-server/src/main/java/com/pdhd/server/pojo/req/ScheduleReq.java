package com.pdhd.server.pojo.req;

import com.pdhd.server.common.enums.RepeatRuleEnum;
import com.pdhd.server.common.enums.TypeEnum;
import com.pdhd.server.common.enums.ZoneTypeEnum;
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

    @NotNull(message = "计划类型不能为空")
    private TypeEnum type;
    @NotNull(message = "难易程度不能为空")
    private ZoneTypeEnum zone;

    private Long goalId;
    
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;
    
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;
    
    private RepeatRuleEnum repeatRule;
    private String customDays;
    private String groupId;
}

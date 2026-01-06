package com.pdhd.server.pojo.req;

import com.pdhd.server.common.enums.RepeatRuleEnum;
import com.pdhd.server.common.enums.TypeEnum;
import com.pdhd.server.common.enums.ZoneTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.LocalTime;

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

    // 循环计划的时间范围 (HH:mm:ss)
    private LocalTime startTime;
    private LocalTime endTime;

    // 单次计划的时间范围 (yyyy-MM-dd HH:mm:ss)
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    @NotNull(message = "重复规则不能为空")
    private RepeatRuleEnum repeatRuleType;
    private String repeatRuleConfig;
    private String groupId;
}

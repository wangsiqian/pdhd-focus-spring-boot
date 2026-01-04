package com.pdhd.server.pojo.resp;

import com.pdhd.server.common.enums.RepeatRuleEnum;
import com.pdhd.server.common.enums.TypeEnum;
import com.pdhd.server.common.enums.ZoneTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author pdhd
 */
@Data
public class ScheduleDTO {
    private Long id;
    private String title;
    private String content;
    private TypeEnum type;
    private ZoneTypeEnum zone;
    private Long goalId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private RepeatRuleEnum repeatRule;
    private String customDays;
    private String groupId;
    private Long userId;
    private LocalDateTime createdAt;
}
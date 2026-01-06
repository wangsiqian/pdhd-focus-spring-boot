package com.pdhd.server.pojo.resp;

import com.pdhd.server.common.enums.RepeatRuleEnum;
import com.pdhd.server.common.enums.TypeEnum;
import com.pdhd.server.common.enums.ZoneTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @author pdhd
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDTO {
    private Long id;
    private String title;
    private String content;
    private TypeEnum type;
    private ZoneTypeEnum zone;
    private Long goalId;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Integer status;
    private Boolean completed;
    private RepeatRuleEnum repeatRuleType;
    private String repeatRuleConfig;
    private String groupId;
    private Long userId;
    private LocalDateTime createdAt;
}

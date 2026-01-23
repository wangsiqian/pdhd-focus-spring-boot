package com.adhd.server.pojo.resp;

import com.adhd.server.common.enums.TypeEnum;
import com.adhd.server.common.enums.ZoneTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author adhd
 */
@Data
public class ActivityDTO {
    private Long id;
    private Long scheduleId;
    private String title;
    private String content;
    private TypeEnum type;
    private ZoneTypeEnum zone;
    private Long goalId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Long userId;
    private LocalDateTime createdAt;
}
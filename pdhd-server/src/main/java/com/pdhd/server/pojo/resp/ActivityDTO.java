package com.pdhd.server.pojo.resp;

import com.pdhd.server.common.enums.TypeEnum;
import com.pdhd.server.common.enums.ZoneTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author pdhd
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
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long userId;
    private LocalDateTime createdAt;
}
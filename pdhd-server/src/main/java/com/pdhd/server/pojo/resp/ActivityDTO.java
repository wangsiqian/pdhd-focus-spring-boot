package com.pdhd.server.pojo.resp;

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
    private String type;
    private String zone;
    private Long goalId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long userId;
    private LocalDateTime createdAt;
}
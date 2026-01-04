package com.pdhd.server.req;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author pdhd
 */
@Data
public class ListScheduleReq {
    private Long goalId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean fullDetail;
}
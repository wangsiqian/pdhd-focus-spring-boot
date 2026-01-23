package com.adhd.server.pojo.req;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author adhd
 */
@Data
public class ListActivityReq {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean fullDetail;
}
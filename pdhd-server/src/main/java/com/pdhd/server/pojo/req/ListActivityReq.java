package com.pdhd.server.pojo.req;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author pdhd
 */
@Data
public class ListActivityReq {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean fullDetail;
}
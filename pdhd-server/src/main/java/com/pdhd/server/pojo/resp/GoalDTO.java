package com.pdhd.server.pojo.resp;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author pdhd
 */
@Data
public class GoalDTO {
    private Long id;
    private String title;
    private String color;
    private Integer status;
    private Integer progress;
    private Long userId;
    private LocalDateTime createdAt;
}

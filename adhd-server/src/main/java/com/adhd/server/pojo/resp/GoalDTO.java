package com.adhd.server.pojo.resp;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author adhd
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
    private LocalDateTime updatedAt;
    private List<ScheduleDTO> repeatSchedules;
}

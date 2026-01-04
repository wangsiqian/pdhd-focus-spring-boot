package com.pdhd.server.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author pdhd
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private String type;
    private String zone;
    private Long goalId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private String repeatRule;
    private String customDays;
    private String groupId;
    private Long userId;

    @TableLogic(value = "0", delval = "1")
    private Boolean isDelete;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
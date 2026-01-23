package com.adhd.server.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.adhd.server.common.enums.TypeEnum;
import com.adhd.server.common.enums.ZoneTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author adhd
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Activity {
    @TableId(type = IdType.AUTO)
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

    @TableLogic(value = "0", delval = "1")
    private Boolean isDelete;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
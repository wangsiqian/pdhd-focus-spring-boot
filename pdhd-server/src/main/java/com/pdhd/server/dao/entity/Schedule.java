package com.pdhd.server.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.pdhd.server.common.enums.RepeatRuleEnum;
import com.pdhd.server.common.enums.TypeEnum;
import com.pdhd.server.common.enums.ZoneTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

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
    private TypeEnum type;
    private ZoneTypeEnum zone;
    private Long goalId;
    private LocalTime startTime;
    private LocalTime endTime;

    // 单次计划的时间范围
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private RepeatRuleEnum repeatRuleType;
    private String repeatRuleConfig;
    private String groupId;
    private Long userId;

    @TableLogic(value = "0", delval = "1")
    private Boolean isDelete;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

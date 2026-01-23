package com.adhd.server.pojo.resp;

import com.adhd.server.common.enums.TypeEnum;
import com.adhd.server.common.enums.ZoneTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author wangsiqian
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FocusTaskResp {
    private Long id;
    private Long scheduleId;
    private String title;
    private String content;
    private TypeEnum type;
    private ZoneTypeEnum zone;
    private Long goalId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Boolean isResuming;
}

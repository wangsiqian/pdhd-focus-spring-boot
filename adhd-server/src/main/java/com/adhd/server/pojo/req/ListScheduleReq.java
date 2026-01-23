package com.adhd.server.pojo.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author adhd
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListScheduleReq {
    private Long goalId;

    @NotNull
    private LocalDateTime startDateTime;
    @NotNull
    private LocalDateTime endDateTime;
    private Boolean fullDetail;
}
package com.adhd.server.pojo.req;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author adhd
 */
@Data
public class CompleteScheduleReq {
  @NotNull
  private Long scheduleId;
  private LocalDateTime startDateTime;
  private LocalDateTime endDateTime;
}

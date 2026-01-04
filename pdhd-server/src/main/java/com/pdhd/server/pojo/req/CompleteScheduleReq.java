package com.pdhd.server.pojo.req;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author pdhd
 */
@Data
public class CompleteScheduleReq {
  @NotNull
  private Long scheduleId;
  private LocalDateTime startDateTime;
  private LocalDateTime endDateTime;
}

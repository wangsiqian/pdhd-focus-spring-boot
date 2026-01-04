package com.pdhd.server.pojo.req;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author pdhd
 */
@Data
public class UncompleteScheduleReq {
  @NotNull
  private Long scheduleId;
  @NotNull
  private LocalDateTime startDateTime;
}

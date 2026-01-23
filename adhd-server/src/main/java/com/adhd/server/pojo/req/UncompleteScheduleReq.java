package com.adhd.server.pojo.req;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author adhd
 */
@Data
public class UncompleteScheduleReq {
  @NotNull
  private Long scheduleId;
  @NotNull
  private LocalDateTime startDateTime;
  /**
   * 结束时间，用于处理跨天计划
   */
  private LocalDateTime endDateTime;
}

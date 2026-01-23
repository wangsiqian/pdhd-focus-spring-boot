package com.adhd.server.pojo.req;

import lombok.Data;

import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author adhd
 */
@Data
public class BatchCompleteScheduleReq {
  @NotNull(message = "事项列表不能为空")
  @NotEmpty(message = "事项列表不能为空")
  private List<CompleteScheduleReq> schedules;
}

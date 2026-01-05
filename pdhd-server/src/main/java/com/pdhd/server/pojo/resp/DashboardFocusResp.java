package com.pdhd.server.pojo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wangsiqian
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardFocusResp {
    private FocusTaskResp current;
    private FocusTaskResp next;
}

package com.adhd.server.pojo.resp;

import com.adhd.server.common.enums.TypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分布统计项
 *
 * @author wangsiqian
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistributionItemResp {
    /**
     * 类型
     */
    private TypeEnum type;
    /**
     * 时长（分钟）
     */
    private Long value;
}

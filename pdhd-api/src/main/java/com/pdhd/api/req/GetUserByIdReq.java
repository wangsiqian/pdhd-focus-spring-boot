package com.pdhd.api.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author wangsiqian
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetUserByIdReq {
    @NotNull
    private Long userId;
}

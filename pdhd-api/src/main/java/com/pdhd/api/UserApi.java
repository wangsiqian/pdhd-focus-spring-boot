package com.pdhd.api;

import com.pdhd.api.req.GetUserByIdReq;
import com.pdhd.api.resp.UserResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

/**
 * @author wangsiqian
 */
@Component
@FeignClient(name = "UserApi", url = "http://pdhd-focus:8080")
public interface UserApi {
    @PostMapping("api/v1/users/info")
    UserResp getById(@Valid @RequestBody GetUserByIdReq req);
}

package com.adhd.api;

import com.adhd.api.req.GetUserByIdReq;
import com.adhd.api.resp.UserResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

/**
 * @author wangsiqian
 */
@Component
@FeignClient(name = "UserApi", url = "http://adhd:8080")
public interface UserApi {
    @PostMapping("api/v1/users/info")
    UserResp getById(@Valid @RequestBody GetUserByIdReq req);
}

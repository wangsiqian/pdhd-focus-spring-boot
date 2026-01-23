package com.adhd.server.controller;

import com.adhd.server.common.annotation.EnableApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wangsiqian
 */
@RestController
@EnableApiResponse
public class HealthController {

    /**
     * 服务探活
     */
    @GetMapping("/health/check")
    public String check() {
        return "ok";
    }
}

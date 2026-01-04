package com.pdhd.server.common.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import com.xxl.job.core.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@Slf4j
public class XxlJobConfiguration {
    @Value("${spring.application.name:pdhd-focus}")
    private String appName;
    @Value("${xxl.job.admin.addresses:http://xxl-job:8080/xxl-job-admin}")
    private String adminAddresses;
    @Value("${xxl.job.accessToken:}")
    private String accessToken;
    @Value("${xxl.job.executor.port:9999}")
    private int port;
    @Value("${xxl.job.executor.logpath:}")
    private String logPath;
    @Value("${xxl.job.executor.logretentiondays:-1}")
    private int logRetentionDays;

    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(adminAddresses);
        xxlJobSpringExecutor.setAppname(appName);
        xxlJobSpringExecutor.setIp(IpUtil.getIp());
        xxlJobSpringExecutor.setPort(port);
        xxlJobSpringExecutor.setAccessToken(accessToken);
        xxlJobSpringExecutor.setLogPath(logPath);
        xxlJobSpringExecutor.setLogRetentionDays(logRetentionDays);

        return xxlJobSpringExecutor;
    }
}

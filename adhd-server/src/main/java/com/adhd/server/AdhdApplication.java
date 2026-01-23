package com.adhd.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author wangsiqian
 */
@SpringBootApplication
@EnableAspectJAutoProxy
@EnableTransactionManagement
@ConfigurationPropertiesScan
@EnableFeignClients(basePackages = {"com.adhd"})
public class AdhdApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdhdApplication.class, args);
    }
}

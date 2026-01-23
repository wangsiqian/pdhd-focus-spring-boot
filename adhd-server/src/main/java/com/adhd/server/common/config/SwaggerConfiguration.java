package com.adhd.server.common.config;

import com.adhd.server.common.constant.SpringConstants;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;

/**
 * @author wangsiqian
 */
@Configuration
@Profile({SpringConstants.DEV_PROFILE, SpringConstants.TEST_PROFILE})
public class SwaggerConfiguration extends BaseSwaggerConfiguration {
    @Override
    protected ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("后台接口").description("模板后台接口").version("1.0.0").build();
    }
}

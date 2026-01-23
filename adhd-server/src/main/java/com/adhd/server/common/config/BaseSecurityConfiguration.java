package com.adhd.server.common.config;

import com.adhd.server.common.handler.PermissionDeniedHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

/**
 * @author wangsiqian
 */
@RequiredArgsConstructor
public abstract class BaseSecurityConfiguration extends WebSecurityConfigurerAdapter {
    private final PermissionDeniedHandler permissionDeniedHandler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf()
                .disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .headers()
                .cacheControl();

        http.exceptionHandling()
                .accessDeniedHandler(permissionDeniedHandler)
                .authenticationEntryPoint(permissionDeniedHandler);
    }

    @Override
    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    public AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManagerBean();
    }
}

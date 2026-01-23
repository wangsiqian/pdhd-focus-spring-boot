package com.adhd.server.common.config;

import com.adhd.server.common.enums.UserTypeEnum;
import com.adhd.server.common.filter.AuthenticationFilter;
import com.adhd.server.common.handler.PermissionDeniedHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;

/**
 * @author wangsiqian
 */
@EnableWebSecurity
@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfiguration extends BaseSecurityConfiguration {
    private final AuthenticationFilter authenticationFilter;

    public SecurityConfiguration(
            PermissionDeniedHandler permissionDeniedHandler,
            AuthenticationFilter authenticationFilter) {
        super(permissionDeniedHandler);
        this.authenticationFilter = authenticationFilter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);

        String[] authorities = Arrays.stream(UserTypeEnum.values()).map(Enum::name).toArray(String[]::new);
        http.authorizeRequests().antMatchers(HttpMethod.OPTIONS, "/webApi/**")
                .permitAll()
                .antMatchers("/health/check", "/webApi/users/login", "/api/**").permitAll()
                .antMatchers("/webApi/**").hasAnyAuthority(authorities)
                .anyRequest()
                .authenticated()
                .and()
                .httpBasic();

        http.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }
}

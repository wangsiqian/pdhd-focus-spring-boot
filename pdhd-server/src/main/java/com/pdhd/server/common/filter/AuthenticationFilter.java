package com.pdhd.server.common.filter;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.pdhd.server.common.Details;
import com.pdhd.server.common.constant.SecurityConstant;
import com.pdhd.server.common.enums.UserTypeEnum;
import com.pdhd.server.common.util.JwtUtils;
import com.pdhd.server.dao.entity.User;
import com.pdhd.server.manager.UserManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * @author wangsiqian
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationFilter extends OncePerRequestFilter {
    private final UserManager userManager;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // FIXME: 后续改成调用服务，统一鉴权

        String token = request.getHeader(SecurityConstant.AUTHORIZATION_HEADER);
        if (StrUtil.isNotBlank(token)) {
            try {
                parseToken(request, token);
            } catch (Exception error) {
                log.info("解析Token失败: {}", token);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 解析用户请求头里面的token
     *
     * @param request 请求
     * @param token   token
     * @author wangsiqian
     */
    private void parseToken(HttpServletRequest request, String token) {
        User user = JwtUtils.parseToken(token);
        if (Objects.isNull(user)) {
            return;
        }

        String cachedToken = userManager.getTokenByUserId(user.getId());
        if (StrUtil.isBlank(cachedToken) || ObjectUtil.notEqual(cachedToken, token)) {
            // 判断token是否还在缓存中
            log.info("登录token已失效: {}", user);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, token,
                AuthorityUtils.createAuthorityList(UserTypeEnum.REGISTERED.name()));
        // 将userId传递给下层
        Details details = Details.builder()
                .details(new WebAuthenticationDetailsSource().buildDetails(request))
                .user(user)
                .build();

        authentication.setDetails(details);
        // 将用户信息设置到 context，后面的 filter chain 会进行身份验证
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}

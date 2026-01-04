package com.pdhd.server.manager;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author wangsiqian
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class UserManager {
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 获取账号的session信息
     *
     * @param userId 账号
     * @return session
     * @author wangsiqian
     */
    public String getTokenByUserId(Long userId) {
        return stringRedisTemplate.opsForValue().get(StrUtil.format("pdhd:user:token:{}", userId));
    }
}

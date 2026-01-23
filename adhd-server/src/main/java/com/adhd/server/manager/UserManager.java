package com.adhd.server.manager;

import cn.hutool.core.util.StrUtil;
import com.adhd.server.common.constant.JwtConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

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
        return stringRedisTemplate.opsForValue().get(StrUtil.format("adhd:user:token:{}", userId));
    }

    /**
     * 缓存账号的session信息
     *
     * @param userId 账号
     * @param token  token
     * @author wangsiqian
     */
    public void saveToken(Long userId, String token) {
        stringRedisTemplate.opsForValue()
                .set(StrUtil.format("adhd:user:token:{}", userId), token, JwtConstants.EXPIRATION_MILLIS,
                        TimeUnit.MILLISECONDS);
        log.info("缓存用户token成功，userId：{}", userId);
    }
}

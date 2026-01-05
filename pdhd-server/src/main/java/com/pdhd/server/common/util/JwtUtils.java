package com.pdhd.server.common.util;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.pdhd.server.common.constant.JwtConstants;
import com.pdhd.server.dao.entity.User;
import com.google.common.collect.Maps;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wangsiqian
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JwtUtils {

    public static String generateToken(String subject) {
        return Jwts.builder()
                .setClaims(new HashMap<>(4, 1))
                .setSubject(subject)
                .setExpiration(
                        new Date(System.currentTimeMillis() + JwtConstants.EXPIRATION_MILLIS))
                .signWith(SignatureAlgorithm.HS512, JwtConstants.SECRET)
                .compact();
    }

    public static String generateToken(User user) {
        Map<String, Object> claims = Maps.newHashMap();
        claims.put("id", user.getId());
        claims.put("username", user.getUsername());
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + JwtConstants.EXPIRATION_MILLIS))
                .signWith(SignatureAlgorithm.HS512, JwtConstants.SECRET)
                .compact();
    }

    public static User parseToken(String token) {
        try {
            String subject = JSONObject.toJSONString(Jwts.parser()
                    .setSigningKey(JwtConstants.SECRET.getBytes(StandardCharsets.UTF_8))
                    .parseClaimsJws(token)
                    .getBody());
            return JSONObject.parseObject(subject, User.class, JSONReader.Feature.SupportSmartMatch);
        } catch (Exception error) {
            log.error("解析token失败：token={}", token, error);
            return null;
        }
    }
}

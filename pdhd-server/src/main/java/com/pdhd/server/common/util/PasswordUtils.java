package com.pdhd.server.common.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.nacos.common.utils.MD5Utils;
import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;

/**
 * @author wangsiqian
 */
public final class PasswordUtils {
    private final static String SALT = "adhd";

    @SneakyThrows
    public static String encrypt(String rawPassword) {
        if (StrUtil.isBlank(rawPassword)) {
            return "";
        }

        rawPassword = rawPassword + SALT;
        return MD5Utils.md5Hex(rawPassword.getBytes(StandardCharsets.UTF_8));
    }

    public static boolean matches(String rawPassword, String storedPassword) {
        return StrUtil.equals(encrypt(rawPassword), storedPassword);
    }
}

package com.pdhd.server.common.util;

import com.pdhd.server.common.Details;
import com.pdhd.server.dao.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author wangsiqian
 */
public class ContextUtils {
    /**
     * 获取当前用户
     */
    public static User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Details details = (Details) authentication.getDetails();
        return details.getUser();
    }
}

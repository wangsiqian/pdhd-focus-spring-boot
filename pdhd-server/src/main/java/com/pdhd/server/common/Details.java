package com.pdhd.server.common;

import com.pdhd.server.dao.entity.User;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

/**
 * @author wangsiqian
 */
@Data
@Builder
public class Details {
    private WebAuthenticationDetails details;
    private User user;
}

package com.adhd.server.common;

import com.adhd.server.dao.entity.User;
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

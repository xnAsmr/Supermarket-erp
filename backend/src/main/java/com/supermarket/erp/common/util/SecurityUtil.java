package com.supermarket.erp.common.util;

import com.supermarket.erp.common.exception.BusinessException;
import com.supermarket.erp.common.security.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    private SecurityUtil() {
    }

    public static LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(401, "未登录");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof LoginUser) {
            return (LoginUser) principal;
        }
        throw new BusinessException(401, "未登录");
    }

    public static Long getUserId() {
        return getLoginUser().getId();
    }

    public static Long getTenantId() {
        return getLoginUser().getTenantId();
    }

    public static Long getStoreId() {
        return getLoginUser().getStoreId();
    }

    public static String getUsername() {
        return getLoginUser().getUsername();
    }

    public static String getName() {
        return getLoginUser().getName();
    }
}

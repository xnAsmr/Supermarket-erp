package com.supermarket.erp.common.security;

import com.supermarket.erp.common.util.SecurityUtil;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component("perm")
public class PermissionChecker {

    private static final String ALL_PERMISSION = "*:*:*";

    public boolean check(String permission) {
        Set<String> permissions = SecurityUtil.getLoginUser().getPermissions();
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }
        return permissions.contains(ALL_PERMISSION) || permissions.contains(permission);
    }
}

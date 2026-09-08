package com.supermarket.erp.common.security;

import com.supermarket.erp.module.auth.mapper.UserMapper;
import com.supermarket.erp.module.auth.entity.User;
import com.supermarket.erp.module.system.entity.UserRole;
import com.supermarket.erp.module.system.mapper.MenuMapper;
import com.supermarket.erp.module.system.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final MenuMapper menuMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        LoginUser loginUser = new LoginUser();
        loginUser.setId(user.getId());
        loginUser.setTenantId(user.getTenantId());
        loginUser.setStoreId(user.getStoreId());
        loginUser.setUsername(user.getUsername());
        loginUser.setPassword(user.getPassword());
        loginUser.setName(user.getName());
        loginUser.setPhone(user.getPhone());
        loginUser.setAvatar(user.getAvatar());
        loginUser.setEmail(user.getEmail());
        loginUser.setUserType(user.getUserType());
        loginUser.setLoginCount(user.getLoginCount());

        // 系统管理员(3)拥有全部权限；其他用户从角色关联的按钮权限中加载
        if (user.getUserType() != null && user.getUserType() == 3) {
            loginUser.setPermissions(Set.of("*:*:*"));
            loginUser.setRoleIds(List.of());
        } else {
            List<UserRole> userRoles = userRoleMapper.selectByUserId(user.getId());
            loginUser.setRoleIds(userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList()));
            List<String> perms = menuMapper.selectPermissionCodesByUserId(user.getId());
            loginUser.setPermissions(new HashSet<>(perms));
        }

        return loginUser;
    }
}

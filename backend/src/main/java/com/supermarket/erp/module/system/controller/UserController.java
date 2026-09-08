package com.supermarket.erp.module.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.erp.common.result.PageResult;
import com.supermarket.erp.common.result.Result;
import com.supermarket.erp.common.annotation.LogOperation;
import com.supermarket.erp.module.auth.entity.User;
import com.supermarket.erp.module.auth.mapper.UserMapper;
import com.supermarket.erp.module.system.entity.Role;
import com.supermarket.erp.module.system.entity.UserRole;
import com.supermarket.erp.module.system.mapper.RoleMapper;
import com.supermarket.erp.module.system.mapper.UserRoleMapper;
import com.supermarket.erp.module.tenant.entity.Tenant;
import com.supermarket.erp.module.tenant.mapper.TenantMapper;
import com.supermarket.erp.common.util.SecurityUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/system/users")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final TenantMapper tenantMapper;

    private static final Integer SYSTEM_ADMIN_TYPE = 3;

    @GetMapping
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer userType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long tenantId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        Long currentTenantId = SecurityUtil.getTenantId();
        Integer currentUserType = SecurityUtil.getLoginUser().getUserType();
        boolean isSystemAdmin = SYSTEM_ADMIN_TYPE.equals(currentUserType);

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (isSystemAdmin) {
            if (tenantId != null) {
                wrapper.eq(User::getTenantId, tenantId);
            }
        } else {
            wrapper.eq(User::getTenantId, currentTenantId);
        }

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(User::getUsername, keyword)
                    .or().like(User::getName, keyword)
                    .or().like(User::getPhone, keyword));
        }
        if (userType != null) {
            wrapper.eq(User::getUserType, userType);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> pageResult = userMapper.selectPage(new Page<>(page, pageSize), wrapper);

        List<Long> userIds = pageResult.getRecords().stream().map(User::getId).collect(Collectors.toList());
        Map<Long, Long> userRoleMap = new HashMap<>();
        Map<Long, String> roleNameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            LambdaQueryWrapper<UserRole> urWrapper = new LambdaQueryWrapper<>();
            urWrapper.in(UserRole::getUserId, userIds);
            List<UserRole> userRoles = userRoleMapper.selectList(urWrapper);
            Set<Long> roleIds = new HashSet<>();
            for (UserRole ur : userRoles) {
                userRoleMap.put(ur.getUserId(), ur.getRoleId());
                roleIds.add(ur.getRoleId());
            }
            if (!roleIds.isEmpty()) {
                List<Role> roles = roleMapper.selectBatchIds(roleIds);
                for (Role r : roles) {
                    roleNameMap.put(r.getId(), r.getRoleName());
                }
            }
        }

        List<Map<String, Object>> voList = pageResult.getRecords().stream().map(u -> {
            Map<String, Object> vo = new LinkedHashMap<>();
            vo.put("id", u.getId());
            vo.put("tenantId", u.getTenantId());
            vo.put("username", u.getUsername());
            vo.put("name", u.getName());
            vo.put("phone", u.getPhone());
            vo.put("email", u.getEmail());
            vo.put("storeId", u.getStoreId());
            vo.put("userType", u.getUserType());
            vo.put("status", u.getStatus());
            vo.put("lastLoginTime", u.getLastLoginTime());
            vo.put("lastLoginIp", u.getLastLoginIp());
            vo.put("loginCount", u.getLoginCount());
            vo.put("remark", u.getRemark());
            vo.put("createTime", u.getCreateTime());
            vo.put("updateTime", u.getUpdateTime());
            Long roleId = userRoleMap.get(u.getId());
            vo.put("roleId", roleId);
            vo.put("roleName", roleId != null ? roleNameMap.get(roleId) : null);
            return vo;
        }).collect(Collectors.toList());

        if (isSystemAdmin && !voList.isEmpty()) {
            Set<Long> tenantIds = voList.stream()
                    .map(m -> (Long) m.get("tenantId"))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (!tenantIds.isEmpty()) {
                Map<Long, String> tenantNameMap = new HashMap<>();
                List<Tenant> tenants = tenantMapper.selectBatchIds(tenantIds);
                for (Tenant t : tenants) {
                    tenantNameMap.put(t.getId(), t.getTenantName());
                }
                for (Map<String, Object> vo : voList) {
                    Long tid = (Long) vo.get("tenantId");
                    vo.put("tenantName", tid != null ? tenantNameMap.get(tid) : null);
                }
            }
        }

        PageResult<Map<String, Object>> result = new PageResult<>();
        result.setList(voList);
        result.setTotal(pageResult.getTotal());
        result.setPage(page);
        result.setPageSize(pageSize);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        user.setPassword(null);

        Map<String, Object> vo = new LinkedHashMap<>();
        vo.put("id", user.getId());
        vo.put("tenantId", user.getTenantId());
        vo.put("username", user.getUsername());
        vo.put("name", user.getName());
        vo.put("phone", user.getPhone());
        vo.put("email", user.getEmail());
        vo.put("storeId", user.getStoreId());
        vo.put("userType", user.getUserType());
        vo.put("status", user.getStatus());
        vo.put("lastLoginTime", user.getLastLoginTime());
        vo.put("lastLoginIp", user.getLastLoginIp());
        vo.put("loginCount", user.getLoginCount());
        vo.put("remark", user.getRemark());
        vo.put("createTime", user.getCreateTime());
        vo.put("updateTime", user.getUpdateTime());

        List<UserRole> userRoles = userRoleMapper.selectByUserId(id);
        if (!userRoles.isEmpty()) {
            vo.put("roleId", userRoles.get(0).getRoleId());
            Role role = roleMapper.selectById(userRoles.get(0).getRoleId());
            vo.put("roleName", role != null ? role.getRoleName() : null);
        } else {
            vo.put("roleId", null);
            vo.put("roleName", null);
        }

        return Result.success(vo);
    }

    @PostMapping
    @LogOperation(module = "用户管理", operation = "新增用户")
    @PreAuthorize("@perm.check('sys:user:add')")
    public Result<Long> create(@RequestBody UserCreateDTO dto) {
        // 检查用户名唯一
        LambdaQueryWrapper<User> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(User::getUsername, dto.getUsername());
        if (userMapper.selectCount(checkWrapper) > 0) {
            return Result.fail("用户名已存在");
        }

        Long currentTenantId = SecurityUtil.getTenantId();

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setName(dto.getName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setUserType(dto.getUserType());
        user.setTenantId(dto.getTenantId() != null ? dto.getTenantId() : currentTenantId);
        user.setStoreId(dto.getStoreId());
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);

        if (dto.getRoleId() != null) {
            UserRole userRole = new UserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(dto.getRoleId());
            userRoleMapper.insert(userRole);
        }

        return Result.success(user.getId());
    }

    @PutMapping("/{id}")
    @LogOperation(module = "用户管理", operation = "修改用户")
    @PreAuthorize("@perm.check('sys:user:edit')")
    public Result<Void> update(@PathVariable Long id, @RequestBody UserUpdateDTO dto) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.fail("用户不存在");
        }

        if (StringUtils.hasText(dto.getName())) user.setName(dto.getName());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getUserType() != null) user.setUserType(dto.getUserType());
        if (dto.getTenantId() != null) user.setTenantId(dto.getTenantId());
        if (dto.getStoreId() != null) user.setStoreId(dto.getStoreId());
        if (dto.getStatus() != null) user.setStatus(dto.getStatus());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        if (dto.getRoleId() != null) {
            userRoleMapper.deleteByUserId(id);
            UserRole userRole = new UserRole();
            userRole.setUserId(id);
            userRole.setRoleId(dto.getRoleId());
            userRoleMapper.insert(userRole);
        }

        return Result.success();
    }

    @DeleteMapping("/{id}")
    @LogOperation(module = "用户管理", operation = "删除用户")
    @PreAuthorize("@perm.check('sys:user:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        userMapper.deleteById(id);
        return Result.success();
    }

    @PutMapping("/{id}/reset-password")
    @LogOperation(module = "用户管理", operation = "重置密码")
    @PreAuthorize("@perm.check('sys:user:resetpwd')")
    public Result<Void> resetPassword(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        user.setPassword(passwordEncoder.encode("123456"));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return Result.success();
    }

    @GetMapping("/profile")
    public Result<Map<String, Object>> getProfile() {
        Long userId = SecurityUtil.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        user.setPassword(null);

        Map<String, Object> vo = new LinkedHashMap<>();
        vo.put("id", user.getId());
        vo.put("username", user.getUsername());
        vo.put("name", user.getName());
        vo.put("phone", user.getPhone());
        vo.put("email", user.getEmail());
        vo.put("avatar", user.getAvatar());
        vo.put("userType", user.getUserType());
        vo.put("createTime", user.getCreateTime());

        List<UserRole> userRoles = userRoleMapper.selectByUserId(userId);
        if (!userRoles.isEmpty()) {
            Role role = roleMapper.selectById(userRoles.get(0).getRoleId());
            vo.put("roleName", role != null ? role.getRoleName() : null);
        } else {
            vo.put("roleName", null);
        }

        return Result.success(vo);
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody Map<String, String> params) {
        Long userId = SecurityUtil.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }

        if (params.containsKey("name")) user.setName(params.get("name"));
        if (params.containsKey("phone")) user.setPhone(params.get("phone"));
        if (params.containsKey("email")) user.setEmail(params.get("email"));
        if (params.containsKey("avatar")) user.setAvatar(params.get("avatar"));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return Result.success();
    }

    @PutMapping("/profile/password")
    public Result<Void> updatePassword(@RequestBody Map<String, String> params) {
        Long userId = SecurityUtil.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }

        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        if (!StringUtils.hasText(oldPassword) || !StringUtils.hasText(newPassword)) {
            return Result.fail("请输入旧密码和新密码");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return Result.fail("旧密码不正确");
        }
        if (newPassword.length() < 6) {
            return Result.fail("新密码长度不能少于6位");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return Result.success();
    }

    @Data
    public static class UserCreateDTO {
        private String username;
        private String password;
        private String name;
        private String phone;
        private String email;
        private Integer userType;
        private Long tenantId;
        private Long storeId;
        private Long roleId;
        private Integer status;
    }

    @Data
    public static class UserUpdateDTO {
        private String name;
        private String phone;
        private String email;
        private Integer userType;
        private Long tenantId;
        private Long storeId;
        private Long roleId;
        private Integer status;
    }
}

package com.supermarket.erp.module.system.controller;

import com.supermarket.erp.common.annotation.LogOperation;
import com.supermarket.erp.common.result.PageResult;
import com.supermarket.erp.common.result.Result;
import com.supermarket.erp.common.util.SecurityUtil;
import com.supermarket.erp.module.system.dto.RoleCreateDTO;
import com.supermarket.erp.module.system.service.IRoleService;
import com.supermarket.erp.module.system.vo.RoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "角色管理")
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final IRoleService roleService;

    @Operation(summary = "角色分页列表")
    @GetMapping
    public Result<PageResult<RoleVO>> list(
            @RequestParam(required = false) Long tenantId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        Long currentTenantId = SecurityUtil.getTenantId();
        if (currentTenantId != null) {
            tenantId = currentTenantId;
        }
        return Result.success(roleService.pageList(tenantId, page, pageSize));
    }

    @Operation(summary = "角色详情")
    @GetMapping("/{id}")
    public Result<RoleVO> detail(@PathVariable Long id) {
        return Result.success(roleService.getById(id));
    }

    @Operation(summary = "新增角色")
    @LogOperation(module = "角色管理", operation = "新增角色")
    @PreAuthorize("@perm.check('sys:role:add')")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody RoleCreateDTO dto) {
        Long tenantId = SecurityUtil.getTenantId();
        return Result.success(roleService.create(dto, tenantId));
    }

    @Operation(summary = "修改角色")
    @LogOperation(module = "角色管理", operation = "修改角色")
    @PreAuthorize("@perm.check('sys:role:edit')")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody RoleCreateDTO dto) {
        Long tenantId = SecurityUtil.getTenantId();
        roleService.update(id, dto, tenantId);
        return Result.success();
    }

    @Operation(summary = "删除角色")
    @LogOperation(module = "角色管理", operation = "删除角色")
    @PreAuthorize("@perm.check('sys:role:delete')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long tenantId = SecurityUtil.getTenantId();
        roleService.delete(id, tenantId);
        return Result.success();
    }
}

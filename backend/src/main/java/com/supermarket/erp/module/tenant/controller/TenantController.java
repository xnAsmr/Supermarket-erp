package com.supermarket.erp.module.tenant.controller;

import com.supermarket.erp.common.annotation.LogOperation;
import com.supermarket.erp.common.result.PageResult;
import com.supermarket.erp.common.result.Result;
import com.supermarket.erp.common.util.SecurityUtil;
import com.supermarket.erp.module.tenant.dto.TenantCreateDTO;
import com.supermarket.erp.module.tenant.service.ITenantService;
import com.supermarket.erp.module.tenant.vo.TenantVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "租户管理")
@RestController
@RequestMapping("/api/v1/admin/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final ITenantService tenantService;

    @Operation(summary = "当前租户信息")
    @GetMapping("/current")
    public Result<TenantVO> current() {
        Long tenantId = SecurityUtil.getTenantId();
        if (tenantId == null) {
            return Result.fail("当前用户未绑定租户");
        }
        return Result.success(tenantService.getById(tenantId));
    }

    @Operation(summary = "修改当前租户基本信息")
    @PutMapping("/current")
    public Result<Void> updateCurrent(@Valid @RequestBody TenantCreateDTO dto) {
        Long tenantId = SecurityUtil.getTenantId();
        if (tenantId == null) {
            return Result.fail("当前用户未绑定租户");
        }
        tenantService.updateCurrent(tenantId, dto);
        return Result.success();
    }

    @Operation(summary = "租户统计")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.success(tenantService.getStats());
    }

    @Operation(summary = "租户分页列表")
    @GetMapping
    public Result<PageResult<TenantVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(tenantService.pageList(page, pageSize));
    }

    @Operation(summary = "租户详情")
    @GetMapping("/{id}")
    public Result<TenantVO> detail(@PathVariable Long id) {
        return Result.success(tenantService.getById(id));
    }

    @Operation(summary = "新增租户")
    @LogOperation(module = "租户管理", operation = "新增租户")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody TenantCreateDTO dto) {
        return Result.success(tenantService.create(dto));
    }

    @Operation(summary = "修改租户")
    @LogOperation(module = "租户管理", operation = "修改租户")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody TenantCreateDTO dto) {
        tenantService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "修改租户状态")
    @LogOperation(module = "租户管理", operation = "修改租户状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        tenantService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "删除租户")
    @LogOperation(module = "租户管理", operation = "删除租户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tenantService.delete(id);
        return Result.success();
    }
}

package com.supermarket.erp.module.tenant.controller;

import com.supermarket.erp.common.annotation.LogOperation;
import com.supermarket.erp.common.exception.BusinessException;
import com.supermarket.erp.common.result.PageResult;
import com.supermarket.erp.common.result.Result;
import com.supermarket.erp.common.util.SecurityUtil;
import com.supermarket.erp.module.tenant.dto.StoreCreateDTO;
import com.supermarket.erp.module.tenant.entity.Tenant;
import com.supermarket.erp.module.tenant.mapper.TenantMapper;
import com.supermarket.erp.module.tenant.service.IStoreService;
import com.supermarket.erp.module.tenant.vo.StoreVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "门店管理")
@RestController
@RequestMapping("/api/v1/admin/stores")
@RequiredArgsConstructor
public class StoreController {

    private final IStoreService storeService;
    private final TenantMapper tenantMapper;

    @Operation(summary = "门店分页列表")
    @GetMapping
    public Result<PageResult<StoreVO>> list(
            @RequestParam(required = false) Long tenantId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long currentTenantId = SecurityUtil.getTenantId();
        if (currentTenantId != null) {
            tenantId = currentTenantId;
        }
        return Result.success(storeService.pageList(tenantId, page, pageSize));
    }

    @Operation(summary = "门店详情")
    @GetMapping("/{id}")
    public Result<StoreVO> detail(@PathVariable Long id) {
        return Result.success(storeService.getById(id));
    }

    @Operation(summary = "新增门店")
    @LogOperation(module = "门店管理", operation = "新增门店")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody StoreCreateDTO dto) {
        Long tenantId = SecurityUtil.getTenantId();
        if (tenantId == null) {
            return Result.fail("租户ID不能为空");
        }

        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) {
            return Result.fail("租户不存在");
        }

        long currentStoreCount = storeService.countByTenantId(tenantId);
        if (tenant.getMaxStores() != null && currentStoreCount >= tenant.getMaxStores()) {
            return Result.fail("已达到门店数量上限（" + tenant.getMaxStores() + "家）");
        }

        return Result.success(storeService.create(dto, tenantId));
    }

    @Operation(summary = "修改门店")
    @LogOperation(module = "门店管理", operation = "修改门店")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody StoreCreateDTO dto) {
        storeService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "修改门店状态")
    @LogOperation(module = "门店管理", operation = "修改门店状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        storeService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "删除门店")
    @LogOperation(module = "门店管理", operation = "删除门店")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        storeService.delete(id);
        return Result.success();
    }
}

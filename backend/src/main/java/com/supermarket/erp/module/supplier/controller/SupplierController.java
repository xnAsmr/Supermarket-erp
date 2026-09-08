package com.supermarket.erp.module.supplier.controller;

import com.supermarket.erp.common.annotation.LogOperation;
import com.supermarket.erp.common.result.PageResult;
import com.supermarket.erp.common.result.Result;
import com.supermarket.erp.common.util.SecurityUtil;
import com.supermarket.erp.module.supplier.dto.SupplierCreateDTO;
import com.supermarket.erp.module.supplier.dto.SupplierQueryDTO;
import com.supermarket.erp.module.supplier.service.ISupplierService;
import com.supermarket.erp.module.supplier.vo.SupplierVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "供应商管理")
@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final ISupplierService supplierService;

    @Operation(summary = "供应商分页列表")
    @GetMapping
    public Result<PageResult<SupplierVO>> list(SupplierQueryDTO query) {
        Long tenantId = SecurityUtil.getTenantId();
        query.setTenantId(tenantId);
        return Result.success(supplierService.pageList(query));
    }

    @Operation(summary = "供应商详情")
    @GetMapping("/{id}")
    public Result<SupplierVO> detail(@PathVariable Long id) {
        return Result.success(supplierService.getById(id));
    }

    @Operation(summary = "新增供应商")
    @LogOperation(module = "供应商管理", operation = "新增供应商")
    @PreAuthorize("@perm.check('pms:supplier:add')")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody SupplierCreateDTO dto) {
        Long tenantId = SecurityUtil.getTenantId();
        Long userId = SecurityUtil.getUserId();
        return Result.success(supplierService.create(dto, tenantId, userId));
    }

    @Operation(summary = "修改供应商")
    @LogOperation(module = "供应商管理", operation = "修改供应商")
    @PreAuthorize("@perm.check('pms:supplier:edit')")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody SupplierCreateDTO dto) {
        Long tenantId = SecurityUtil.getTenantId();
        Long userId = SecurityUtil.getUserId();
        supplierService.update(id, dto, tenantId, userId);
        return Result.success();
    }

    @Operation(summary = "删除供应商")
    @LogOperation(module = "供应商管理", operation = "删除供应商")
    @PreAuthorize("@perm.check('pms:supplier:delete')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long tenantId = SecurityUtil.getTenantId();
        supplierService.delete(id, tenantId);
        return Result.success();
    }
}

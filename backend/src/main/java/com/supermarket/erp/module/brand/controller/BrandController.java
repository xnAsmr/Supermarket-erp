package com.supermarket.erp.module.brand.controller;

import com.supermarket.erp.common.annotation.LogOperation;
import com.supermarket.erp.common.result.PageResult;
import com.supermarket.erp.common.result.Result;
import com.supermarket.erp.common.util.SecurityUtil;
import com.supermarket.erp.module.brand.dto.BrandCreateDTO;
import com.supermarket.erp.module.brand.dto.BrandQueryDTO;
import com.supermarket.erp.module.brand.service.IBrandService;
import com.supermarket.erp.module.brand.vo.BrandVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "品牌管理")
@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
public class BrandController {

    private final IBrandService brandService;

    @Operation(summary = "品牌分页列表")
    @GetMapping
    public Result<PageResult<BrandVO>> list(BrandQueryDTO query) {
        Long tenantId = SecurityUtil.getTenantId();
        query.setTenantId(tenantId);
        return Result.success(brandService.pageList(query));
    }

    @Operation(summary = "品牌详情")
    @GetMapping("/{id}")
    public Result<BrandVO> detail(@PathVariable Long id) {
        return Result.success(brandService.getById(id));
    }

    @Operation(summary = "新增品牌")
    @LogOperation(module = "品牌管理", operation = "新增品牌")
    @PreAuthorize("@perm.check('pms:brand:add')")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody BrandCreateDTO dto) {
        Long tenantId = SecurityUtil.getTenantId();
        Long userId = SecurityUtil.getUserId();
        return Result.success(brandService.create(dto, tenantId, userId));
    }

    @Operation(summary = "修改品牌")
    @LogOperation(module = "品牌管理", operation = "修改品牌")
    @PreAuthorize("@perm.check('pms:brand:edit')")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody BrandCreateDTO dto) {
        Long tenantId = SecurityUtil.getTenantId();
        Long userId = SecurityUtil.getUserId();
        brandService.update(id, dto, tenantId, userId);
        return Result.success();
    }

    @Operation(summary = "删除品牌")
    @LogOperation(module = "品牌管理", operation = "删除品牌")
    @PreAuthorize("@perm.check('pms:brand:delete')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long tenantId = SecurityUtil.getTenantId();
        brandService.delete(id, tenantId);
        return Result.success();
    }
}

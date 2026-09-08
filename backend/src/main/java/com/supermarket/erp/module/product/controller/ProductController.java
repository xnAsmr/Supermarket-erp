package com.supermarket.erp.module.product.controller;

import com.supermarket.erp.common.annotation.LogOperation;
import com.supermarket.erp.common.result.PageResult;
import com.supermarket.erp.common.result.Result;
import com.supermarket.erp.common.util.SecurityUtil;
import com.supermarket.erp.module.product.dto.ProductCreateDTO;
import com.supermarket.erp.module.product.dto.ProductQueryDTO;
import com.supermarket.erp.module.product.service.IProductService;
import com.supermarket.erp.module.product.vo.ProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "商品管理")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final IProductService productService;

    @Operation(summary = "商品分页列表")
    @GetMapping
    public Result<PageResult<ProductVO>> list(ProductQueryDTO query) {
        query.setTenantId(SecurityUtil.getTenantId());
        return Result.success(productService.pageList(query));
    }

    @Operation(summary = "商品详情")
    @GetMapping("/{id}")
    public Result<ProductVO> detail(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    @Operation(summary = "新增商品")
    @LogOperation(module = "商品管理", operation = "新增商品")
    @PreAuthorize("@perm.check('pms:product:add')")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody ProductCreateDTO dto) {
        Long tenantId = SecurityUtil.getTenantId();
        Long userId = SecurityUtil.getUserId();
        return Result.success(productService.create(dto, tenantId, userId));
    }

    @Operation(summary = "修改商品")
    @LogOperation(module = "商品管理", operation = "修改商品")
    @PreAuthorize("@perm.check('pms:product:edit')")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ProductCreateDTO dto) {
        Long tenantId = SecurityUtil.getTenantId();
        Long userId = SecurityUtil.getUserId();
        productService.update(id, dto, tenantId, userId);
        return Result.success();
    }

    @Operation(summary = "删除商品")
    @LogOperation(module = "商品管理", operation = "删除商品")
    @PreAuthorize("@perm.check('pms:product:delete')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long tenantId = SecurityUtil.getTenantId();
        productService.delete(id, tenantId);
        return Result.success();
    }

    @Operation(summary = "更新商品状态")
    @LogOperation(module = "商品管理", operation = "更新商品状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Long tenantId = SecurityUtil.getTenantId();
        productService.updateStatus(id, status, tenantId);
        return Result.success();
    }

    @Operation(summary = "POS商品搜索")
    @GetMapping("/search")
    public Result<List<ProductVO>> searchForPos(
            @Parameter(description = "关键词") @RequestParam String keyword) {
        Long tenantId = SecurityUtil.getTenantId();
        return Result.success(productService.searchForPos(keyword, tenantId));
    }

    @Operation(summary = "热门商品")
    @GetMapping("/hot")
    public Result<List<ProductVO>> hotProducts(
            @Parameter(description = "数量") @RequestParam(defaultValue = "20") Integer limit) {
        Long tenantId = SecurityUtil.getTenantId();
        return Result.success(productService.getHotProducts(tenantId, limit));
    }
}

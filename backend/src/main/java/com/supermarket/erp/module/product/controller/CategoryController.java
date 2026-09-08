package com.supermarket.erp.module.product.controller;

import com.supermarket.erp.common.annotation.LogOperation;
import com.supermarket.erp.common.result.Result;
import com.supermarket.erp.common.util.SecurityUtil;
import com.supermarket.erp.module.product.service.ICategoryService;
import com.supermarket.erp.module.product.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "商品分类")
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final ICategoryService categoryService;

    @Operation(summary = "分类树形列表")
    @GetMapping("/tree")
    public Result<List<CategoryVO>> tree() {
        Long tenantId = SecurityUtil.getTenantId();
        return Result.success(categoryService.getTree(tenantId));
    }

    @Operation(summary = "子分类列表")
    @GetMapping("/children")
    public Result<List<CategoryVO>> children(@RequestParam Long parentId) {
        Long tenantId = SecurityUtil.getTenantId();
        return Result.success(categoryService.getChildren(parentId, tenantId));
    }

    @Operation(summary = "新增分类")
    @LogOperation(module = "商品分类", operation = "新增分类")
    @PostMapping
    public Result<Long> create(@RequestParam String name, @RequestParam String code, @RequestParam(required = false) Long parentId) {
        Long tenantId = SecurityUtil.getTenantId();
        return Result.success(categoryService.create(name, code, parentId, tenantId));
    }

    @Operation(summary = "修改分类")
    @LogOperation(module = "商品分类", operation = "修改分类")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestParam String name, @RequestParam String code) {
        Long tenantId = SecurityUtil.getTenantId();
        categoryService.update(id, name, code, tenantId);
        return Result.success();
    }

    @Operation(summary = "删除分类")
    @LogOperation(module = "商品分类", operation = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long tenantId = SecurityUtil.getTenantId();
        categoryService.delete(id, tenantId);
        return Result.success();
    }
}

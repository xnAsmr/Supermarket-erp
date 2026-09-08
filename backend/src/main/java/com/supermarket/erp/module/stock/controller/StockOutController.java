package com.supermarket.erp.module.stock.controller;

import com.supermarket.erp.common.annotation.LogOperation;
import com.supermarket.erp.common.result.PageResult;
import com.supermarket.erp.common.result.Result;
import com.supermarket.erp.common.util.SecurityUtil;
import com.supermarket.erp.module.stock.dto.StockOutCreateDTO;
import com.supermarket.erp.module.stock.service.IStockOutService;
import com.supermarket.erp.module.stock.vo.StockOutVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "出库管理")
@RestController
@RequestMapping("/api/v1/stock-out")
@RequiredArgsConstructor
public class StockOutController {

    private final IStockOutService stockOutService;

    @Operation(summary = "出库单列表")
    @GetMapping
    public Result<PageResult<StockOutVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long tenantId = SecurityUtil.getTenantId();
        return Result.success(stockOutService.pageList(page, pageSize, tenantId));
    }

    @Operation(summary = "出库单详情")
    @GetMapping("/{id}")
    public Result<StockOutVO> detail(@PathVariable Long id) {
        return Result.success(stockOutService.getById(id));
    }

    @Operation(summary = "创建出库单")
    @LogOperation(module = "库存管理", operation = "创建出库单")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody StockOutCreateDTO dto) {
        Long tenantId = SecurityUtil.getTenantId();
        Long userId = SecurityUtil.getUserId();
        return Result.success(stockOutService.create(dto, tenantId, userId));
    }

    @Operation(summary = "编辑出库单")
    @LogOperation(module = "库存管理", operation = "编辑出库单")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody StockOutCreateDTO dto) {
        Long tenantId = SecurityUtil.getTenantId();
        stockOutService.update(id, dto, tenantId);
        return Result.success();
    }

    @Operation(summary = "删除出库单")
    @LogOperation(module = "库存管理", operation = "删除出库单")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long tenantId = SecurityUtil.getTenantId();
        stockOutService.delete(id, tenantId);
        return Result.success();
    }

    @Operation(summary = "确认出库")
    @LogOperation(module = "库存管理", operation = "确认出库")
    @PutMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id) {
        Long tenantId = SecurityUtil.getTenantId();
        Long userId = SecurityUtil.getUserId();
        stockOutService.confirm(id, tenantId, userId);
        return Result.success();
    }

    @Operation(summary = "撤销出库")
    @LogOperation(module = "库存管理", operation = "撤销出库")
    @PutMapping("/{id}/rollback-confirm")
    public Result<Void> rollbackConfirm(@PathVariable Long id) {
        Long tenantId = SecurityUtil.getTenantId();
        Long userId = SecurityUtil.getUserId();
        stockOutService.rollbackConfirm(id, tenantId, userId);
        return Result.success();
    }
}

package com.supermarket.erp.module.stock.controller;

import com.supermarket.erp.common.annotation.LogOperation;
import com.supermarket.erp.common.result.PageResult;
import com.supermarket.erp.common.result.Result;
import com.supermarket.erp.common.util.SecurityUtil;
import com.supermarket.erp.module.stock.dto.PurchaseInCreateDTO;
import com.supermarket.erp.module.stock.dto.StockQueryDTO;
import com.supermarket.erp.module.stock.service.IPurchaseInService;
import com.supermarket.erp.module.stock.service.IStockCheckService;
import com.supermarket.erp.module.stock.service.IStockService;
import com.supermarket.erp.module.stock.vo.PurchaseInVO;
import com.supermarket.erp.module.stock.vo.StockDetailVO;
import com.supermarket.erp.module.stock.vo.StockVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "库存管理")
@RestController
@RequestMapping("/api/v1/stock")
@RequiredArgsConstructor
public class StockController {

    private final IStockService stockService;
    private final IPurchaseInService purchaseInService;
    private final IStockCheckService stockCheckService;

    @Operation(summary = "库存列表")
    @GetMapping
    public Result<PageResult<StockVO>> list(StockQueryDTO query) {
        Long tenantId = SecurityUtil.getTenantId();
        return Result.success(stockService.pageList(query, tenantId));
    }

    @Operation(summary = "库存详情")
    @GetMapping("/detail")
    public Result<StockDetailVO> detail(@RequestParam Long productId, @RequestParam(required = false) Long storeId) {
        Long tenantId = SecurityUtil.getTenantId();
        return Result.success(stockService.getDetail(tenantId, productId, storeId));
    }

    @Operation(summary = "采购入库单列表")
    @GetMapping("/purchase-in")
    public Result<?> purchaseInList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long tenantId = SecurityUtil.getTenantId();
        return Result.success(purchaseInService.pageList(page, pageSize, tenantId));
    }

    @Operation(summary = "采购入库单详情")
    @GetMapping("/purchase-in/{id}")
    public Result<PurchaseInVO> purchaseInDetail(@PathVariable Long id) {
        return Result.success(purchaseInService.getById(id));
    }

    @Operation(summary = "创建采购入库单")
    @LogOperation(module = "库存管理", operation = "创建采购入库单")
    @PostMapping("/purchase-in")
    public Result<Long> createPurchaseIn(@Valid @RequestBody PurchaseInCreateDTO dto) {
        Long tenantId = SecurityUtil.getTenantId();
        Long userId = SecurityUtil.getUserId();
        return Result.success(purchaseInService.create(dto, tenantId, userId));
    }

    @Operation(summary = "编辑采购入库单")
    @LogOperation(module = "库存管理", operation = "编辑采购入库单")
    @PutMapping("/purchase-in/{id}")
    public Result<Void> updatePurchaseIn(@PathVariable Long id, @Valid @RequestBody PurchaseInCreateDTO dto) {
        Long tenantId = SecurityUtil.getTenantId();
        purchaseInService.update(id, dto, tenantId);
        return Result.success();
    }

    @Operation(summary = "审核采购入库单")
    @LogOperation(module = "库存管理", operation = "审核采购入库单")
    @PutMapping("/purchase-in/{id}/review")
    public Result<Void> reviewPurchaseIn(@PathVariable Long id) {
        Long tenantId = SecurityUtil.getTenantId();
        Long userId = SecurityUtil.getUserId();
        purchaseInService.review(id, tenantId, userId);
        return Result.success();
    }

    @Operation(summary = "删除采购入库单")
    @LogOperation(module = "库存管理", operation = "删除采购入库单")
    @DeleteMapping("/purchase-in/{id}")
    public Result<Void> deletePurchaseIn(@PathVariable Long id) {
        Long tenantId = SecurityUtil.getTenantId();
        purchaseInService.delete(id, tenantId);
        return Result.success();
    }

    @Operation(summary = "撤销审核")
    @LogOperation(module = "库存管理", operation = "撤销审核")
    @PutMapping("/purchase-in/{id}/revoke-review")
    public Result<Void> revokeReview(@PathVariable Long id) {
        Long tenantId = SecurityUtil.getTenantId();
        purchaseInService.revokeReview(id, tenantId);
        return Result.success();
    }

    @Operation(summary = "采购入库确认")
    @LogOperation(module = "库存管理", operation = "采购入库确认")
    @PutMapping("/purchase-in/{id}/stock-in")
    public Result<Void> stockIn(@PathVariable Long id) {
        Long tenantId = SecurityUtil.getTenantId();
        Long userId = SecurityUtil.getUserId();
        purchaseInService.stockIn(id, tenantId, userId);
        return Result.success();
    }

    @Operation(summary = "回退入库")
    @LogOperation(module = "库存管理", operation = "回退入库")
    @PutMapping("/purchase-in/{id}/rollback-stock-in")
    public Result<Void> rollbackStockIn(@PathVariable Long id) {
        Long tenantId = SecurityUtil.getTenantId();
        Long userId = SecurityUtil.getUserId();
        purchaseInService.rollbackStockIn(id, tenantId, userId);
        return Result.success();
    }

    @Operation(summary = "创建盘点单")
    @LogOperation(module = "库存管理", operation = "创建盘点单")
    @PostMapping("/check")
    public Result<Long> createCheck(@RequestParam Long storeId) {
        Long tenantId = SecurityUtil.getTenantId();
        Long userId = SecurityUtil.getUserId();
        return Result.success(stockCheckService.createCheck(storeId, tenantId, userId));
    }

    @Operation(summary = "完成盘点")
    @LogOperation(module = "库存管理", operation = "完成盘点")
    @PutMapping("/check/{id}/complete")
    public Result<Void> completeCheck(@PathVariable Long id) {
        Long tenantId = SecurityUtil.getTenantId();
        Long userId = SecurityUtil.getUserId();
        stockCheckService.completeCheck(id, tenantId, userId);
        return Result.success();
    }

    @Operation(summary = "库存调整（手动出入库）")
    @LogOperation(module = "库存管理", operation = "库存调整")
    @PostMapping("/adjust")
    public Result<Void> adjustStock(@RequestParam Long storeId,
                                    @RequestParam Long productId,
                                    @RequestParam Integer quantity,
                                    @RequestParam String type) {
        Long tenantId = SecurityUtil.getTenantId();
        Long userId = SecurityUtil.getUserId();
        stockService.adjustStock(tenantId, storeId, productId, quantity, type, userId);
        return Result.success();
    }
}

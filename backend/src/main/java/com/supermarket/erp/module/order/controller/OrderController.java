package com.supermarket.erp.module.order.controller;

import com.supermarket.erp.common.annotation.LogOperation;
import com.supermarket.erp.common.result.PageResult;
import com.supermarket.erp.common.result.Result;
import com.supermarket.erp.common.util.SecurityUtil;
import com.supermarket.erp.module.order.dto.OrderQueryDTO;
import com.supermarket.erp.module.order.dto.PaymentFlowQueryDTO;
import com.supermarket.erp.module.order.mapper.OrderPaymentMapper;
import com.supermarket.erp.module.order.service.IOrderService;
import com.supermarket.erp.module.order.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "订单管理")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final IOrderService orderService;
    private final OrderPaymentMapper orderPaymentMapper;

    @Operation(summary = "分页查询订单列表")
    @GetMapping
    public Result<PageResult<OrderVO>> pageList(OrderQueryDTO query) {
        Long tenantId = SecurityUtil.getTenantId();
        return Result.success(orderService.pageList(query, tenantId));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    public Result<OrderVO> detail(@PathVariable Long id) {
        Long tenantId = SecurityUtil.getTenantId();
        return Result.success(orderService.getById(id, tenantId));
    }

    @Operation(summary = "支付方式余额统计")
    @GetMapping("/payment-method-stats")
    public Result<List<PaymentMethodStatVO>> paymentMethodStats() {
        Long tenantId = SecurityUtil.getTenantId();
        return Result.success(orderPaymentMapper.selectPaymentMethodStats(tenantId));
    }

    @Operation(summary = "每日支付流水趋势")
    @GetMapping("/daily-payment-flow")
    public Result<List<DailyPaymentFlowVO>> dailyPaymentFlow(PaymentFlowQueryDTO query) {
        Long tenantId = SecurityUtil.getTenantId();
        String startDate = query.getStartDate() != null ? query.getStartDate() + " 00:00:00" : "";
        String endDate = query.getEndDate() != null ? query.getEndDate() + " 23:59:59" : "";
        return Result.success(orderPaymentMapper.selectDailyPaymentFlow(tenantId, startDate, endDate));
    }

    @Operation(summary = "财务日结（按支付方式汇总）")
    @GetMapping("/daily-settlement")
    public Result<List<DailyPaymentFlowVO>> dailySettlement(
            @RequestParam String date) {
        Long tenantId = SecurityUtil.getTenantId();
        String startDate = date + " 00:00:00";
        String endDate = date + " 23:59:59";
        return Result.success(orderPaymentMapper.selectDailyPaymentFlow(tenantId, startDate, endDate));
    }

    @Operation(summary = "支付流水明细统计")
    @GetMapping("/payment-flow")
    public Result<List<PaymentFlowVO>> paymentFlow(PaymentFlowQueryDTO query) {
        Long tenantId = SecurityUtil.getTenantId();
        String startDate = query.getStartDate() != null ? query.getStartDate() + " 00:00:00" : "";
        String endDate = query.getEndDate() != null ? query.getEndDate() + " 23:59:59" : "";
        List<PaymentFlowVO> list = orderPaymentMapper.selectPaymentFlow(
                tenantId, startDate, endDate, query.getStoreId(), query.getPaymentMethod());
        return Result.success(list);
    }

    @Operation(summary = "取消订单")
    @LogOperation(module = "订单管理", operation = "取消订单")
    @PreAuthorize("@perm.check('oms:order:cancel')")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        Long tenantId = SecurityUtil.getTenantId();
        Long userId = SecurityUtil.getUserId();
        orderService.cancel(id, tenantId, userId);
        return Result.success();
    }

    @Operation(summary = "完成订单")
    @LogOperation(module = "订单管理", operation = "完成订单")
    @PreAuthorize("@perm.check('oms:order:complete')")
    @PutMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable Long id) {
        Long tenantId = SecurityUtil.getTenantId();
        Long userId = SecurityUtil.getUserId();
        orderService.complete(id, tenantId, userId);
        return Result.success();
    }

    @Operation(summary = "退款")
    @LogOperation(module = "订单管理", operation = "订单退款")
    @PreAuthorize("@perm.check('oms:order:refund')")
    @PutMapping("/{id}/refund")
    public Result<Void> refund(@PathVariable Long id) {
        Long tenantId = SecurityUtil.getTenantId();
        Long userId = SecurityUtil.getUserId();
        orderService.refund(id, tenantId, userId);
        return Result.success();
    }

    @Operation(summary = "删除订单")
    @LogOperation(module = "订单管理", operation = "删除订单")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long tenantId = SecurityUtil.getTenantId();
        Long userId = SecurityUtil.getUserId();
        orderService.delete(id, tenantId, userId);
        return Result.success();
    }

    @Operation(summary = "修改订单备注")
    @LogOperation(module = "订单管理", operation = "修改订单备注")
    @PutMapping("/{id}/remark")
    public Result<Void> updateRemark(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        Long tenantId = SecurityUtil.getTenantId();
        Long userId = SecurityUtil.getUserId();
        orderService.updateRemark(id, tenantId, body.get("remark"), userId);
        return Result.success();
    }
}

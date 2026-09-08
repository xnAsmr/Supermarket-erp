package com.supermarket.erp.module.payment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supermarket.erp.common.exception.BusinessException;
import com.supermarket.erp.common.result.Result;
import com.supermarket.erp.common.util.SecurityUtil;
import com.supermarket.erp.module.system.entity.PaymentMethod;
import com.supermarket.erp.module.system.mapper.PaymentMethodMapper;
import com.supermarket.erp.module.system.service.IPaymentMethodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Tag(name = "支付方式")
@RestController
@RequestMapping("/api/v1/payment-methods")
@RequiredArgsConstructor
public class PaymentController {

    private final IPaymentMethodService paymentMethodService;
    private final PaymentMethodMapper paymentMethodMapper;

    @Operation(summary = "获取支付方式列表")
    @GetMapping
    public Result<List<PaymentMethod>> list() {
        Long tenantId = SecurityUtil.getTenantId();
        return Result.success(paymentMethodService.listAll(tenantId));
    }

    @Operation(summary = "新增支付方式")
    @PostMapping
    public Result<Long> create(@RequestBody PaymentMethod method) {
        Long tenantId = SecurityUtil.getTenantId();
        return Result.success(paymentMethodService.create(method, tenantId));
    }

    @Operation(summary = "修改支付方式")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody PaymentMethod method) {
        Long tenantId = SecurityUtil.getTenantId();
        method.setId(id);
        paymentMethodService.update(method, tenantId);
        return Result.success();
    }

    @Operation(summary = "删除支付方式")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long tenantId = SecurityUtil.getTenantId();
        paymentMethodService.delete(id, tenantId);
        return Result.success();
    }

    @Operation(summary = "充值/调整余额")
    @PutMapping("/{id}/recharge")
    public Result<Void> recharge(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long tenantId = SecurityUtil.getTenantId();
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        String remark = (String) body.getOrDefault("remark", "");

        PaymentMethod method = paymentMethodMapper.selectOne(
                new LambdaQueryWrapper<PaymentMethod>()
                        .eq(PaymentMethod::getId, id)
                        .eq(PaymentMethod::getTenantId, tenantId)
                        .eq(PaymentMethod::getDeleted, 0)
        );
        if (method == null) {
            throw new BusinessException("支付方式不存在");
        }

        BigDecimal newBalance = method.getBalance().add(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("余额不能为负数");
        }

        method.setBalance(newBalance);
        paymentMethodMapper.updateById(method);
        return Result.success();
    }
}

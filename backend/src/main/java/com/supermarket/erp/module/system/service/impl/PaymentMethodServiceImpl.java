package com.supermarket.erp.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supermarket.erp.common.exception.BusinessException;
import com.supermarket.erp.module.system.entity.PaymentMethod;
import com.supermarket.erp.module.system.mapper.PaymentMethodMapper;
import com.supermarket.erp.module.system.service.IPaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentMethodServiceImpl implements IPaymentMethodService {

    private final PaymentMethodMapper paymentMethodMapper;

    @Override
    public List<PaymentMethod> listAll(Long tenantId) {
        return paymentMethodMapper.selectList(
                new LambdaQueryWrapper<PaymentMethod>()
                        .eq(PaymentMethod::getTenantId, tenantId)
                        .orderByAsc(PaymentMethod::getSort)
        );
    }

    @Override
    public Long create(PaymentMethod method, Long tenantId) {
        method.setTenantId(tenantId);
        paymentMethodMapper.insert(method);
        return method.getId();
    }

    @Override
    public void update(PaymentMethod method, Long tenantId) {
        PaymentMethod existing = paymentMethodMapper.selectById(method.getId());
        if (existing == null || !existing.getTenantId().equals(tenantId)) {
            throw new BusinessException("支付方式不存在");
        }
        paymentMethodMapper.updateById(method);
    }

    @Override
    public void delete(Long id, Long tenantId) {
        PaymentMethod existing = paymentMethodMapper.selectById(id);
        if (existing == null || !existing.getTenantId().equals(tenantId)) {
            throw new BusinessException("支付方式不存在");
        }
        paymentMethodMapper.deleteById(id);
    }
}

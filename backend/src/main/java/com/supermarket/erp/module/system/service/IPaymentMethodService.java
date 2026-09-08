package com.supermarket.erp.module.system.service;

import com.supermarket.erp.module.system.entity.PaymentMethod;

import java.util.List;

public interface IPaymentMethodService {

    List<PaymentMethod> listAll(Long tenantId);

    Long create(PaymentMethod method, Long tenantId);

    void update(PaymentMethod method, Long tenantId);

    void delete(Long id, Long tenantId);
}

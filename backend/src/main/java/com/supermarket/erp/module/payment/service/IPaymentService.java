package com.supermarket.erp.module.payment.service;

import com.supermarket.erp.module.payment.vo.PaymentMethodVO;

import java.util.List;

public interface IPaymentService {

    List<PaymentMethodVO> getPaymentMethods();
}

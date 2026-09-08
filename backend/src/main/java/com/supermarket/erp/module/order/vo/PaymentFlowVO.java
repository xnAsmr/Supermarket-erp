package com.supermarket.erp.module.order.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentFlowVO {

    private String payDate;

    private String paymentMethod;

    private String paymentMethodName;

    private BigDecimal totalAmount;

    private BigDecimal receivedAmount;

    private Integer transactionCount;
}

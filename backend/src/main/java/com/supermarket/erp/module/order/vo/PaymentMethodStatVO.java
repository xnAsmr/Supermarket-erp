package com.supermarket.erp.module.order.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentMethodStatVO {

    private String paymentMethod;

    private String paymentMethodName;

    private BigDecimal totalInflow;

    private BigDecimal totalOutflow;

    private BigDecimal balance;

    private Integer totalInCount;

    private Integer totalOutCount;
}

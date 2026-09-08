package com.supermarket.erp.module.order.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DailyPaymentFlowVO {

    private String payDate;

    private String paymentMethod;

    private String paymentMethodName;

    private BigDecimal inflow;

    private BigDecimal outflow;

    private Integer inflowCount;

    private Integer outflowCount;
}

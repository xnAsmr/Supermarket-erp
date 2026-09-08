package com.supermarket.erp.module.order.dto;

import lombok.Data;

@Data
public class PaymentFlowQueryDTO {

    private String startDate;

    private String endDate;

    private Long storeId;

    private String paymentMethod;
}

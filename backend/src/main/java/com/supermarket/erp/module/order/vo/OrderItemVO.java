package com.supermarket.erp.module.order.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemVO {

    private Long id;

    private Long productId;

    private String productName;

    private String barcode;

    private String spec;

    private String unit;

    private Integer quantity;

    private BigDecimal salePrice;

    private BigDecimal discountPrice;

    private BigDecimal totalPrice;

    private BigDecimal costAmount;

    private BigDecimal profitAmount;

    private Integer isWeight;
}

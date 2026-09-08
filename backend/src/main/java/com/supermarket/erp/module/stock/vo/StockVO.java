package com.supermarket.erp.module.stock.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockVO {

    private Long id;

    private Long tenantId;

    private Long storeId;

    private String storeName;

    private Long productId;

    private String productName;

    private String barcode;

    private BigDecimal warehouseQuantity;

    private BigDecimal shelfQuantity;

    private BigDecimal pendingInQuantity;

    private BigDecimal pendingOutQuantity;

    private BigDecimal avgCost;
}

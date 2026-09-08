package com.supermarket.erp.module.product.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductVO {

    private Long id;
    private String productNo;
    private String barcode;
    private String name;
    private String shortName;
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
    private Long supplierId;
    private String supplierName;
    private String spec;
    private String unit;
    private BigDecimal purchasePrice;
    private BigDecimal salePrice;
    private BigDecimal vipPrice;
    private BigDecimal weight;
    private String image;
    private Integer status;
    private Integer isWeight;
    private Integer isPromotion;
    private Long saleCount;
    private Integer shelfQuantity;
    private Integer warehouseQuantity;
    private LocalDateTime createTime;
}

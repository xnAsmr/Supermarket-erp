package com.supermarket.erp.module.stock.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PurchaseInVO {

    private Long id;

    private String inNo;

    private Long supplierId;

    private String supplierName;

    private Long storeId;

    private String storeName;

    private BigDecimal totalAmount;

    private Integer totalQuantity;

    private Integer status;

    private String operatorName;

    private String reviewerName;

    private LocalDateTime reviewTime;

    private String reviewRemark;

    private String stockInUserName;

    private LocalDateTime stockInTime;

    private List<PurchaseInItemVO> items;

    private LocalDateTime createTime;

    @Data
    public static class PurchaseInItemVO {

        private Long id;

        private Long productId;

        private String productName;

        private Integer quantity;

        private BigDecimal purchasePrice;

        private BigDecimal totalPrice;

        private String batchNo;

        private LocalDateTime expireTime;
    }
}

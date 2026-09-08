package com.supermarket.erp.module.stock.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StockOutVO {

    private Long id;

    private String outNo;

    private Integer outType;

    private Long storeId;

    private String storeName;

    private Long targetStoreId;

    private String targetStoreName;

    private Integer totalQuantity;

    private Integer status;

    private String operatorName;

    private Long reviewerId;

    private String reviewerName;

    private LocalDateTime reviewTime;

    private Long stockOutUserId;

    private String stockOutUserName;

    private LocalDateTime stockOutTime;

    private String remark;

    private List<StockOutItemVO> items;

    private LocalDateTime createTime;

    @Data
    public static class StockOutItemVO {

        private Long id;

        private Long productId;

        private String productName;

        private Integer quantity;

        private String remark;
    }
}

package com.supermarket.erp.module.stock.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StockDetailVO {

    private Long productId;
    private String productName;
    private String barcode;
    private String spec;
    private String unit;

    private Long storeId;
    private String storeName;
    private BigDecimal warehouseQuantity;
    private BigDecimal shelfQuantity;
    private BigDecimal pendingInQuantity;
    private BigDecimal pendingOutQuantity;
    private BigDecimal avgCost;

    private List<StockLogVO> logs;

    @Data
    public static class StockLogVO {
        private Long id;
        private Integer logType;
        private String logTypeName;
        private BigDecimal quantity;
        private BigDecimal beforeStock;
        private BigDecimal afterStock;
        private String relatedNo;
        private String operatorName;
        private LocalDateTime createTime;
    }
}

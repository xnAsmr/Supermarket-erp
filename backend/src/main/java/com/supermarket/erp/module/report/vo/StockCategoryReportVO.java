package com.supermarket.erp.module.report.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockCategoryReportVO {

    private Long id;

    private String categoryName;

    private Integer productCount;

    private BigDecimal totalStock;

    private BigDecimal totalValue;

    private Integer lowStockCount;

    private BigDecimal monthlyInValue;

    private BigDecimal monthlyOutValue;
}

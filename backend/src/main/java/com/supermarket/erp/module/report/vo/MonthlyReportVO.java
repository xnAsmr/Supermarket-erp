package com.supermarket.erp.module.report.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MonthlyReportVO {

    private Integer statYear;

    private Integer statMonth;

    private Integer orderCount;

    private BigDecimal totalAmount;

    private BigDecimal discountAmount;

    private BigDecimal payAmount;

    private BigDecimal costAmount;

    private BigDecimal profitAmount;
}

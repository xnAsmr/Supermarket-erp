package com.supermarket.erp.module.report.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DailyReportVO {

    private LocalDate statDate;

    private Integer orderCount;

    private BigDecimal totalAmount;

    private BigDecimal discountAmount;

    private BigDecimal payAmount;

    private BigDecimal costAmount;

    private BigDecimal profitAmount;

    private BigDecimal cashAmount;

    private BigDecimal wechatAmount;

    private BigDecimal alipayAmount;

    private Integer memberCount;
}

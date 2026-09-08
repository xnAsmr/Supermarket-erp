package com.supermarket.erp.module.report.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardVO {

    private BigDecimal todaySalesAmount;

    private Integer todayOrderCount;

    private Integer todayMemberCount;

    private BigDecimal monthSalesAmount;

    private Integer monthOrderCount;

    private BigDecimal monthProfitAmount;

    private Integer lowStockCount;
}

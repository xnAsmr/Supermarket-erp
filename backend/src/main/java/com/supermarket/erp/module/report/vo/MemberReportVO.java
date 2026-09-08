package com.supermarket.erp.module.report.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MemberReportVO {

    private Integer totalMembers;

    private Integer newMembersThisMonth;

    private Integer activeMembers;

    private BigDecimal totalBalance;

    private BigDecimal totalConsume;
}

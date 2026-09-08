package com.supermarket.erp.module.member.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MemberConsumeLogVO {

    private Long id;

    private String orderNo;

    private BigDecimal amount;

    private BigDecimal payAmount;

    private String payMethod;

    private String payMethodName;

    private Integer orderStatus;

    private String orderStatusName;

    private LocalDateTime orderTime;

    private String storeName;
}

package com.supermarket.erp.module.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVO {

    private Long id;

    private String orderNo;

    private Integer orderType;

    private Long memberId;

    private String memberName;

    private BigDecimal totalAmount;

    private BigDecimal discountAmount;

    private BigDecimal payAmount;

    private BigDecimal profitAmount;

    private String payMethod;

    private String payMethodName;

    private Integer payStatus;

    private Integer orderStatus;

    private Integer totalQuantity;

    private String cashierName;

    private LocalDateTime payTime;

    private LocalDateTime createTime;

    private List<OrderItemVO> items;
}

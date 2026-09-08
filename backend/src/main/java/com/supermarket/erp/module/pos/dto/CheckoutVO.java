package com.supermarket.erp.module.pos.dto;

import com.supermarket.erp.module.order.vo.OrderItemVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CheckoutVO {

    private Long orderId;

    private String orderNo;

    private BigDecimal totalAmount;

    private BigDecimal discountAmount;

    private BigDecimal payAmount;

    private BigDecimal changeAmount;

    private List<OrderItemVO> items;
}

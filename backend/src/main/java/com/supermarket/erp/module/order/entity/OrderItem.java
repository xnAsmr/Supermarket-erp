package com.supermarket.erp.module.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("oms_order_item")
public class OrderItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long productId;

    private String productName;

    private String barcode;

    private String spec;

    private String unit;

    private Integer quantity;

    private BigDecimal salePrice;

    private BigDecimal discountPrice;

    private BigDecimal totalPrice;

    private BigDecimal costAmount;

    private Integer isWeight;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

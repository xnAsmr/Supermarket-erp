package com.supermarket.erp.module.stock.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("wms_purchase_in_item")
public class PurchaseInItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long inId;

    private Long productId;

    private Integer quantity;

    private BigDecimal purchasePrice;

    private BigDecimal totalPrice;

    private String batchNo;

    private LocalDateTime expireTime;
}

package com.supermarket.erp.module.stock.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("wms_stock_out_item")
public class StockOutItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long outId;

    private Long productId;

    private Integer quantity;

    private String remark;
}

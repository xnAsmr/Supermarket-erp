package com.supermarket.erp.module.stock.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("wms_stock_check_item")
public class StockCheckItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long checkId;

    private Long productId;

    private Integer systemStock;

    private Integer actualStock;

    private Integer difference;
}

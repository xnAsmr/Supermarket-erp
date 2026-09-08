package com.supermarket.erp.module.stock.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("wms_stock_log")
public class StockLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    private Long storeId;

    private Long productId;

    private Integer logType;

    private BigDecimal quantity;

    private BigDecimal beforeStock;

    private BigDecimal afterStock;

    private String relatedNo;

    private Long relatedId;

    private Long operatorId;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

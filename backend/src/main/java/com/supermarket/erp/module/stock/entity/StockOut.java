package com.supermarket.erp.module.stock.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wms_stock_out")
public class StockOut {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    private String outNo;

    /** 1=销售, 2=调拨, 3=报损, 4=其他 */
    private Integer outType;

    private Long storeId;

    private Long targetStoreId;

    private Long orderId;

    private Integer totalQuantity;

    private Integer status;

    private Long operatorId;

    private Long reviewerId;

    private LocalDateTime reviewTime;

    private Long stockOutUserId;

    private LocalDateTime stockOutTime;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}

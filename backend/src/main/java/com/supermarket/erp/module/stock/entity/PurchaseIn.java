package com.supermarket.erp.module.stock.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("wms_purchase_in")
public class PurchaseIn {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    private String inNo;

    private Long supplierId;

    private Long storeId;

    private BigDecimal totalAmount;

    private Integer totalQuantity;

    /** 0=草稿, 1=已审核, 2=已入库 */
    private Integer status;

    private Long operatorId;

    private Long reviewerId;

    private LocalDateTime reviewTime;

    private String reviewRemark;

    private Long stockInUserId;

    private LocalDateTime stockInTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}

package com.supermarket.erp.module.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("oms_order")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    private Long storeId;

    private String orderNo;

    private Integer orderType;

    private Integer orderStatus;

    private BigDecimal totalAmount;

    private BigDecimal discountAmount;

    private BigDecimal payAmount;

    private BigDecimal profitAmount;

    private BigDecimal changeAmount;

    private String payMethod;

    @TableField("is_combined_pay")
    private Integer isCombinedPay;

    private Integer payStatus;

    private Long memberId;

    private Long operatorId;

    private LocalDateTime orderTime;

    private LocalDateTime payTime;

    private LocalDateTime completeTime;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @TableLogic
    private Integer deleted;
}

package com.supermarket.erp.module.member.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ums_member_recharge_log")
public class MemberRechargeLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    private Long memberId;

    private String memberName;

    private Integer type;

    private BigDecimal amount;

    private BigDecimal giftAmount;

    private BigDecimal beforeBalance;

    private BigDecimal afterBalance;

    private String payMethod;

    private String relatedOrderNo;

    private Long operatorId;

    private String operatorName;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

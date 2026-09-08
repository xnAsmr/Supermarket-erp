package com.supermarket.erp.module.member.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ums_member_level")
public class MemberLevel {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    private String name;

    private String code;

    private Integer minPoints;

    private BigDecimal discount;

    private String description;

    private Integer sort;

    private Integer status;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}

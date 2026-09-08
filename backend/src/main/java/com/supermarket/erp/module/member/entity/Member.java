package com.supermarket.erp.module.member.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ums_member")
public class Member {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    private String memberNo;

    private String name;

    private String phone;

    private Integer gender;

    private LocalDateTime birthday;

    private Long levelId;

    private Integer points;

    private BigDecimal balance;

    private BigDecimal totalConsume;

    private Integer totalPoints;

    private String email;

    private String address;

    private Integer status;

    private LocalDateTime registerTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private Long updateBy;

    @TableLogic
    private Integer deleted;
}

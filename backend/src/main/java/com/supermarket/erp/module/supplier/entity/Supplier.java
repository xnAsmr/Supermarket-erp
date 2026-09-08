package com.supermarket.erp.module.supplier.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pms_supplier")
public class Supplier {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    private String supplierNo;

    private String name;

    private String contact;

    private String phone;

    private String address;

    private String bankAccount;

    private String bankName;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}

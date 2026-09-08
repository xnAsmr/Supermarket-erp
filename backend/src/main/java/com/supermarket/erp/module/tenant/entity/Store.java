package com.supermarket.erp.module.tenant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_store")
public class Store {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String storeNo;

    private String storeName;

    private Long tenantId;

    private String address;

    private String contactPhone;

    private Long managerId;

    private Integer storeType;

    private String businessHours;

    private String logo;

    private Integer status;

    private Integer sort;

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

package com.supermarket.erp.module.tenant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_tenant")
public class Tenant {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String tenantNo;

    private String tenantName;

    private String contactName;

    private String contactPhone;

    private String contactEmail;

    private String logo;

    private String address;

    private String licenseNo;

    private Integer planType;

    private Integer maxStores;

    private Integer maxUsers;

    private LocalDateTime expireTime;

    private Integer status;

    private Long adminUserId;

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

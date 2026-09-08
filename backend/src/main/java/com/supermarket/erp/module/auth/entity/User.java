package com.supermarket.erp.module.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    private String username;

    private String password;

    private String name;

    private String phone;

    private String email;

    private String avatar;

    private Long storeId;

    private Integer userType;

    private Integer status;

    private LocalDateTime lastLoginTime;

    private String lastLoginIp;

    private Integer loginCount;

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

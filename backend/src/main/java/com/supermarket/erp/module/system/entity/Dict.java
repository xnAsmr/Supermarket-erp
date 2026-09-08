package com.supermarket.erp.module.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_dict")
public class Dict {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String dictType;

    private String dictCode;

    private String dictName;

    private String dictValue;

    private Integer sort;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

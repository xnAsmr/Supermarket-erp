package com.supermarket.erp.module.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pms_product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    private String productNo;

    private String barcode;

    private String name;

    private String pinyin;

    private String shortName;

    private Long categoryId;

    private Long brandId;

    private Long supplierId;

    private String spec;

    private String unit;

    private BigDecimal purchasePrice;

    private BigDecimal salePrice;

    private BigDecimal vipPrice;

    private BigDecimal weight;

    private String image;

    private Integer status;

    private Integer isWeight;

    private Integer isPromotion;

    private Long saleCount;

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

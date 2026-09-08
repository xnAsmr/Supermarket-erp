package com.supermarket.erp.module.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductCreateDTO {

    @NotBlank(message = "商品条码不能为空")
    private String barcode;

    @NotBlank(message = "商品名称不能为空")
    private String name;

    private String shortName;

    @NotNull(message = "商品分类不能为空")
    private Long categoryId;

    private Long brandId;

    private Long supplierId;

    private String spec;

    private String unit;

    @NotNull(message = "采购价不能为空")
    private BigDecimal purchasePrice;

    @NotNull(message = "销售价不能为空")
    private BigDecimal salePrice;

    private BigDecimal vipPrice;

    private BigDecimal weight;

    private String image;

    private String oldImage;

    private Integer isWeight;

    private Integer isPromotion;
}

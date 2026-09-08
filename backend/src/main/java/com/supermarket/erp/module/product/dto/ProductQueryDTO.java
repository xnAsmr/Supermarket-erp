package com.supermarket.erp.module.product.dto;

import lombok.Data;

@Data
public class ProductQueryDTO {

    private String keyword;

    private Long categoryId;

    private Integer status;

    private Integer isPromotion;

    private Long tenantId;

    private Integer page = 1;

    private Integer pageSize = 20;
}

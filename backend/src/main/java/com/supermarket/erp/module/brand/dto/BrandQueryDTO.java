package com.supermarket.erp.module.brand.dto;

import lombok.Data;

@Data
public class BrandQueryDTO {

    private Long tenantId;

    private String keyword;

    private Integer status;

    private Integer page = 1;

    private Integer pageSize = 20;
}

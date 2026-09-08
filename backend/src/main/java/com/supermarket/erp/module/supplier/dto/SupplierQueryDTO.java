package com.supermarket.erp.module.supplier.dto;

import lombok.Data;

@Data
public class SupplierQueryDTO {

    private Long tenantId;

    private String keyword;

    private Integer status;

    private Integer page = 1;

    private Integer pageSize = 20;
}

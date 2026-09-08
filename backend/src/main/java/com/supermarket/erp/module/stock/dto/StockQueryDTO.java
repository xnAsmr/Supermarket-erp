package com.supermarket.erp.module.stock.dto;

import lombok.Data;

@Data
public class StockQueryDTO {

    private Long storeId;

    private Long productId;

    private Long categoryId;

    /** 0=全部 1=有库存 2=无库存 */
    private Integer stockStatus;

    private String keyword;

    private Integer page = 1;

    private Integer pageSize = 20;
}

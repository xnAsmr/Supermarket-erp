package com.supermarket.erp.module.stock.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class StockOutCreateDTO {

    @NotNull(message = "出库类型不能为空")
    private Integer outType;

    @NotNull(message = "门店不能为空")
    private Long storeId;

    private Long targetStoreId;

    private String remark;

    private List<StockOutItemDTO> items;

    @Data
    public static class StockOutItemDTO {

        @NotNull(message = "商品不能为空")
        private Long productId;

        @NotNull(message = "数量不能为空")
        private Integer quantity;

        private String remark;
    }
}

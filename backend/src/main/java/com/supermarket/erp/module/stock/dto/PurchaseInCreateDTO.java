package com.supermarket.erp.module.stock.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PurchaseInCreateDTO {

    @NotNull(message = "供应商不能为空")
    private Long supplierId;

    @NotNull(message = "门店不能为空")
    private Long storeId;

    private List<PurchaseInItemDTO> items;

    @Data
    public static class PurchaseInItemDTO {

        @NotNull(message = "商品不能为空")
        private Long productId;

        @NotNull(message = "数量不能为空")
        private Integer quantity;

        @NotNull(message = "采购价不能为空")
        private BigDecimal purchasePrice;

        private String batchNo;

        private LocalDateTime expireTime;
    }
}

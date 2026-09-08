package com.supermarket.erp.module.pos.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CheckoutDTO {

    @NotNull(message = "门店ID不能为空")
    private Long storeId;

    private Long memberId;

    @NotEmpty(message = "商品列表不能为空")
    private List<CheckoutItemDTO> items;

    @NotEmpty(message = "支付方式不能为空")
    private List<PaymentDetailDTO> payments;

    @Data
    public static class CheckoutItemDTO {

        @NotNull(message = "商品ID不能为空")
        private Long productId;

        @NotNull(message = "数量不能为空")
        private Integer quantity;

        @NotNull(message = "销售价格不能为空")
        private BigDecimal salePrice;

        private Integer isWeight;
    }

        @Data
        public static class PaymentDetailDTO {

            @NotNull(message = "支付方式不能为空")
            private String paymentMethod;

            @NotNull(message = "支付金额不能为空")
            private BigDecimal amount;

            private BigDecimal receivedAmount;
        }
}

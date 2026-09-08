package com.supermarket.erp.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatusEnum {

    PENDING(0, "待支付"),
    PAID(1, "已支付"),
    COMPLETED(2, "已完成"),
    CANCELLED(3, "已取消"),
    REFUNDED(4, "已退款");

    private final Integer code;
    private final String name;

    public static OrderStatusEnum getByCode(Integer code) {
        for (OrderStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}

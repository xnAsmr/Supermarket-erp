package com.supermarket.erp.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentMethodEnum {

    CASH(1, "现金"),
    WECHAT(2, "微信"),
    ALIPAY(3, "支付宝"),
    UNIONPAY(4, "银联"),
    STORED_VALUE(5, "储值卡");

    private final Integer code;
    private final String name;

    public static PaymentMethodEnum getByCode(Integer code) {
        for (PaymentMethodEnum method : values()) {
            if (method.getCode().equals(code)) {
                return method;
            }
        }
        return null;
    }
}

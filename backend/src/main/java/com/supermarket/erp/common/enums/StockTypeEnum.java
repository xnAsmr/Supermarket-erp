package com.supermarket.erp.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StockTypeEnum {

    IN(1, "采购入库"),
    OUT_SALE(2, "销售出库"),
    OUT_TRANSFER(3, "调拨出库"),
    OUT_DAMAGE(4, "报损出库"),
    ADJUST(5, "盘点调整"),
    RETURN(6, "退货入库");

    private final Integer code;
    private final String name;

    public static StockTypeEnum getByCode(Integer code) {
        for (StockTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}

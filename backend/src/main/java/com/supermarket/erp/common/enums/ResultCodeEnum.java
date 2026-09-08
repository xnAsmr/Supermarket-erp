package com.supermarket.erp.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCodeEnum {

    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),

    // 参数校验 400
    BAD_REQUEST(400, "请求参数错误"),

    // 认证相关 401
    UNAUTHORIZED(401, "未认证，请先登录"),
    TOKEN_EXPIRED(401, "Token已过期"),
    TOKEN_INVALID(401, "Token无效"),

    // 权限相关 403
    FORBIDDEN(403, "无权限访问"),

    // 资源不存在 404
    NOT_FOUND(404, "资源不存在"),

    // 业务冲突 409
    CONFLICT(409, "业务冲突"),
    STOCK_INSUFFICIENT(409, "库存不足"),
    DUPLICATE_SUBMIT(409, "重复提交"),

    // 用户相关 1001-1999
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_DISABLED(1002, "用户已禁用"),
    USERNAME_OR_PASSWORD_ERROR(1003, "用户名或密码错误"),
    USERNAME_DUPLICATE(1004, "用户名已存在"),
    STORE_LIMIT_EXCEEDED(1005, "门店数量已达上限"),
    USER_LIMIT_EXCEEDED(1006, "用户数量已达上限"),

    // 商品相关 2001-2999
    PRODUCT_NOT_FOUND(2001, "商品不存在"),
    PRODUCT_BARCODE_DUPLICATE(2002, "商品条码已存在"),
    CATEGORY_HAS_CHILDREN(2003, "该分类下有子分类，无法删除"),
    CATEGORY_HAS_PRODUCTS(2004, "该分类下有商品，无法删除"),

    // 库存相关 3001-3999
    STOCK_NOT_FOUND(3001, "库存记录不存在"),
    STOCK_CHECK_STATUS_ERROR(3002, "盘点状态不正确"),

    // 订单相关 4001-4999
    ORDER_NOT_FOUND(4001, "订单不存在"),
    ORDER_STATUS_ERROR(4002, "订单状态不正确"),
    PAYMENT_AMOUNT_MISMATCH(4003, "支付金额与订单金额不一致"),

    // 会员相关 5001-5999
    MEMBER_NOT_FOUND(5001, "会员不存在"),
    MEMBER_PHONE_DUPLICATE(5002, "手机号已被注册"),
    MEMBER_BALANCE_INSUFFICIENT(5003, "储值余额不足"),
    MEMBER_POINTS_INSUFFICIENT(5004, "积分余额不足"),

    // 租户相关 6001-6999
    TENANT_NOT_FOUND(6001, "租户不存在"),
    TENANT_EXPIRED(6002, "租户已过期"),
    TENANT_DISABLED(6003, "租户已禁用"),

    // 系统相关 7001-7999
    SYS_ERROR(7001, "系统错误"),
    DATA_ERROR(7002, "数据异常");

    private final Integer code;
    private final String message;
}

package com.supermarket.erp.module.supplier.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SupplierVO {

    private Long id;
    private String supplierNo;
    private String name;
    private String contact;
    private String phone;
    private String address;
    private String bankAccount;
    private String bankName;
    private Integer status;
    private LocalDateTime createTime;
}

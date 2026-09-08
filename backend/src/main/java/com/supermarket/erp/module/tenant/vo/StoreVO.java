package com.supermarket.erp.module.tenant.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StoreVO {

    private Long id;
    private String storeNo;
    private String storeName;
    private Long tenantId;
    private String address;
    private String contactPhone;
    private Long managerId;
    private Integer storeType;
    private String businessHours;
    private String logo;
    private Integer status;
    private Integer sort;
    private String remark;
    private LocalDateTime createTime;
}

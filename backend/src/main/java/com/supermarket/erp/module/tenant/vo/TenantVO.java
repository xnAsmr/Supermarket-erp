package com.supermarket.erp.module.tenant.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TenantVO {

    private Long id;
    private String tenantNo;
    private String tenantName;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String address;
    private String licenseNo;
    private Integer planType;
    private Integer maxStores;
    private Integer maxUsers;
    private LocalDateTime expireTime;
    private Integer status;
    private Long adminUserId;
    private String remark;
    private LocalDateTime createTime;
    private Integer storeCount;
}

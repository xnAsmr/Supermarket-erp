package com.supermarket.erp.module.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TenantCreateDTO {

    @NotBlank(message = "租户名称不能为空")
    private String tenantName;

    private String contactName;

    private String contactPhone;

    private String contactEmail;

    private String address;

    private String licenseNo;

    private Integer planType;

    private Integer maxStores;

    private Integer maxUsers;

    private String expireTime;

    private String remark;
}

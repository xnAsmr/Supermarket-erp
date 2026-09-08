package com.supermarket.erp.module.system.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoleVO {

    private Long id;

    private Long tenantId;

    private String roleName;

    private String roleCode;

    private String description;

    private Integer status;

    private List<Long> menuIds;
}

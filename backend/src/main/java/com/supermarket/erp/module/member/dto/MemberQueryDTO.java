package com.supermarket.erp.module.member.dto;

import lombok.Data;

@Data
public class MemberQueryDTO {

    private Long tenantId;

    private String keyword;

    private Long levelId;

    private Integer status;

    private Integer page = 1;

    private Integer pageSize = 20;
}

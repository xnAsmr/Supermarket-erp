package com.supermarket.erp.module.brand.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BrandVO {

    private Long id;
    private String name;
    private String logo;
    private String description;
    private Integer status;
    private Integer sort;
    private LocalDateTime createTime;
}

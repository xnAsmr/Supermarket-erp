package com.supermarket.erp.module.product.vo;

import lombok.Data;

import java.util.List;

@Data
public class CategoryVO {

    private Long id;
    private String name;
    private String code;
    private Long parentId;
    private Integer level;
    private Integer sort;
    private String icon;
    private Integer status;
    private List<CategoryVO> children;
}

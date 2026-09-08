package com.supermarket.erp.module.system.vo;

import lombok.Data;

import java.util.List;

@Data
public class MenuVO {

    private Long id;

    private Long parentId;

    private String menuName;

    private Integer menuType;

    private String path;

    private String component;

    private String permission;

    private String icon;

    private Integer sort;

    private Integer visible;

    private Integer status;

    private List<MenuVO> children;
}

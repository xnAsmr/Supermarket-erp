package com.supermarket.erp.module.system.vo;

import lombok.Data;

@Data
public class DictVO {

    private Long id;

    private String dictType;

    private String dictCode;

    private String dictName;

    private String dictValue;

    private Integer sort;

    private Integer status;
}

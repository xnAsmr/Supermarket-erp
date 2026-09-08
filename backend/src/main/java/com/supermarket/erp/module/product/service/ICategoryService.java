package com.supermarket.erp.module.product.service;

import com.supermarket.erp.module.product.vo.CategoryVO;

import java.util.List;

public interface ICategoryService {

    List<CategoryVO> getTree(Long tenantId);

    List<CategoryVO> getChildren(Long parentId, Long tenantId);

    Long create(String name, String code, Long parentId, Long tenantId);

    void update(Long id, String name, String code, Long tenantId);

    void delete(Long id, Long tenantId);
}

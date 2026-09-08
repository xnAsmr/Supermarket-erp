package com.supermarket.erp.module.product.service;

import com.supermarket.erp.common.result.PageResult;
import com.supermarket.erp.module.product.dto.ProductCreateDTO;
import com.supermarket.erp.module.product.dto.ProductQueryDTO;
import com.supermarket.erp.module.product.vo.ProductVO;

import java.util.List;

public interface IProductService {

    ProductVO getById(Long id);

    PageResult<ProductVO> pageList(ProductQueryDTO query);

    List<ProductVO> searchForPos(String keyword, Long tenantId);

    List<ProductVO> getHotProducts(Long tenantId, Integer limit);

    Long create(ProductCreateDTO dto, Long tenantId, Long userId);

    void update(Long id, ProductCreateDTO dto, Long tenantId, Long userId);

    void delete(Long id, Long tenantId);

    void updateStatus(Long id, Integer status, Long tenantId);
}

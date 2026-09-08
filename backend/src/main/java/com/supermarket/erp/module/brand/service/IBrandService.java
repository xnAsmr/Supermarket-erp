package com.supermarket.erp.module.brand.service;

import com.supermarket.erp.common.result.PageResult;
import com.supermarket.erp.module.brand.dto.BrandCreateDTO;
import com.supermarket.erp.module.brand.dto.BrandQueryDTO;
import com.supermarket.erp.module.brand.vo.BrandVO;

public interface IBrandService {

    BrandVO getById(Long id);

    PageResult<BrandVO> pageList(BrandQueryDTO query);

    Long create(BrandCreateDTO dto, Long tenantId, Long userId);

    void update(Long id, BrandCreateDTO dto, Long tenantId, Long userId);

    void delete(Long id, Long tenantId);
}

package com.supermarket.erp.module.tenant.service;

import com.supermarket.erp.common.result.PageResult;
import com.supermarket.erp.module.tenant.dto.StoreCreateDTO;
import com.supermarket.erp.module.tenant.vo.StoreVO;

public interface IStoreService {

    PageResult<StoreVO> pageList(Long tenantId, Integer page, Integer pageSize);

    StoreVO getById(Long id);

    Long create(StoreCreateDTO dto, Long tenantId);

    void update(Long id, StoreCreateDTO dto);

    void updateStatus(Long id, Integer status);

    void delete(Long id);

    long countByTenantId(Long tenantId);
}

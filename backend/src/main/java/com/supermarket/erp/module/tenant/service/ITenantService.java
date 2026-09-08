package com.supermarket.erp.module.tenant.service;

import com.supermarket.erp.common.result.PageResult;
import com.supermarket.erp.module.tenant.dto.TenantCreateDTO;
import com.supermarket.erp.module.tenant.vo.TenantVO;

import java.util.Map;

public interface ITenantService {

    PageResult<TenantVO> pageList(Integer page, Integer pageSize);

    TenantVO getById(Long id);

    Long create(TenantCreateDTO dto);

    void update(Long id, TenantCreateDTO dto);

    void updateCurrent(Long id, TenantCreateDTO dto);

    void updateStatus(Long id, Integer status);

    void delete(Long id);

    Map<String, Object> getStats();
}

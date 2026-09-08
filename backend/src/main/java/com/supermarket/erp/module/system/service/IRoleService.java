package com.supermarket.erp.module.system.service;

import com.supermarket.erp.common.result.PageResult;
import com.supermarket.erp.module.system.dto.RoleCreateDTO;
import com.supermarket.erp.module.system.vo.RoleVO;

public interface IRoleService {

    PageResult<RoleVO> pageList(Long tenantId, Integer page, Integer pageSize);

    RoleVO getById(Long id);

    Long create(RoleCreateDTO dto, Long tenantId);

    void update(Long id, RoleCreateDTO dto, Long tenantId);

    void delete(Long id, Long tenantId);
}

package com.supermarket.erp.module.supplier.service;

import com.supermarket.erp.common.result.PageResult;
import com.supermarket.erp.module.supplier.dto.SupplierCreateDTO;
import com.supermarket.erp.module.supplier.dto.SupplierQueryDTO;
import com.supermarket.erp.module.supplier.vo.SupplierVO;

public interface ISupplierService {

    SupplierVO getById(Long id);

    PageResult<SupplierVO> pageList(SupplierQueryDTO query);

    Long create(SupplierCreateDTO dto, Long tenantId, Long userId);

    void update(Long id, SupplierCreateDTO dto, Long tenantId, Long userId);

    void delete(Long id, Long tenantId);
}

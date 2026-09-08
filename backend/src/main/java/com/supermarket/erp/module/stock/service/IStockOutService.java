package com.supermarket.erp.module.stock.service;

import com.supermarket.erp.common.result.PageResult;
import com.supermarket.erp.module.stock.dto.StockOutCreateDTO;
import com.supermarket.erp.module.stock.vo.StockOutVO;

public interface IStockOutService {

    PageResult<StockOutVO> pageList(Integer page, Integer pageSize, Long tenantId);

    StockOutVO getById(Long id);

    Long create(StockOutCreateDTO dto, Long tenantId, Long userId);

    void update(Long id, StockOutCreateDTO dto, Long tenantId);

    void delete(Long id, Long tenantId);

    void confirm(Long id, Long tenantId, Long userId);

    void rollbackConfirm(Long id, Long tenantId, Long userId);
}

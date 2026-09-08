package com.supermarket.erp.module.stock.service;

import com.supermarket.erp.common.result.PageResult;
import com.supermarket.erp.module.stock.dto.StockQueryDTO;
import com.supermarket.erp.module.stock.vo.StockDetailVO;
import com.supermarket.erp.module.stock.vo.StockVO;

public interface IStockService {

    PageResult<StockVO> pageList(StockQueryDTO query, Long tenantId);

    StockDetailVO getDetail(Long tenantId, Long productId, Long storeId);

    StockVO getStockByProduct(Long tenantId, Long storeId, Long productId);

    void adjustStock(Long tenantId, Long storeId, Long productId, Integer quantity, String type, Long userId);
}

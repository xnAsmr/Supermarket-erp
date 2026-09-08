package com.supermarket.erp.module.stock.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.erp.module.stock.dto.PurchaseInCreateDTO;
import com.supermarket.erp.module.stock.vo.PurchaseInVO;

public interface IPurchaseInService {

    PurchaseInVO getById(Long id);

    Page<PurchaseInVO> pageList(Integer page, Integer pageSize, Long tenantId);

    Long create(PurchaseInCreateDTO dto, Long tenantId, Long userId);

    void review(Long id, Long tenantId, Long userId);

    void revokeReview(Long id, Long tenantId);

    void update(Long id, PurchaseInCreateDTO dto, Long tenantId);

    void delete(Long id, Long tenantId);

    void stockIn(Long id, Long tenantId, Long userId);

    void rollbackStockIn(Long id, Long tenantId, Long userId);
}

package com.supermarket.erp.module.stock.service;

public interface IStockCheckService {

    Long createCheck(Long storeId, Long tenantId, Long userId);

    void completeCheck(Long id, Long tenantId, Long userId);
}

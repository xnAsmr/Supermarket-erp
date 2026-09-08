package com.supermarket.erp.module.stock.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supermarket.erp.common.exception.BusinessException;
import com.supermarket.erp.module.stock.entity.Stock;
import com.supermarket.erp.module.stock.entity.StockCheck;
import com.supermarket.erp.module.stock.entity.StockCheckItem;
import com.supermarket.erp.module.stock.entity.StockLog;
import com.supermarket.erp.module.stock.mapper.StockCheckItemMapper;
import com.supermarket.erp.module.stock.mapper.StockCheckMapper;
import com.supermarket.erp.module.stock.mapper.StockLogMapper;
import com.supermarket.erp.module.stock.mapper.StockMapper;
import com.supermarket.erp.module.stock.service.IStockCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockCheckServiceImpl implements IStockCheckService {

    private final StockCheckMapper stockCheckMapper;
    private final StockCheckItemMapper stockCheckItemMapper;
    private final StockMapper stockMapper;
    private final StockLogMapper stockLogMapper;

    @Override
    public Long createCheck(Long storeId, Long tenantId, Long userId) {
        StockCheck stockCheck = new StockCheck();
        stockCheck.setTenantId(tenantId);
        stockCheck.setStoreId(storeId);
        stockCheck.setCheckNo("SC" + System.currentTimeMillis());
        stockCheck.setStatus(0);
        stockCheck.setOperatorId(userId);
        stockCheckMapper.insert(stockCheck);

        LambdaQueryWrapper<Stock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Stock::getTenantId, tenantId)
                .eq(Stock::getStoreId, storeId);
        List<Stock> stocks = stockMapper.selectList(wrapper);

        int totalSystem = 0;
        for (Stock stock : stocks) {
            StockCheckItem item = new StockCheckItem();
            item.setCheckId(stockCheck.getId());
            item.setProductId(stock.getProductId());
            int warehouseQty = stock.getWarehouseQuantity() != null ? stock.getWarehouseQuantity().intValue() : 0;
            item.setSystemStock(warehouseQty);
            item.setActualStock(0);
            item.setDifference(0);
            stockCheckItemMapper.insert(item);
            totalSystem += warehouseQty;
        }

        stockCheck.setTotalSystem(totalSystem);
        stockCheck.setTotalActual(0);
        stockCheck.setTotalDiff(0);
        stockCheckMapper.updateById(stockCheck);

        return stockCheck.getId();
    }

    @Override
    @Transactional
    public void completeCheck(Long id, Long tenantId, Long userId) {
        StockCheck stockCheck = stockCheckMapper.selectById(id);
        if (stockCheck == null) {
            throw new BusinessException(3201, "盘点单不存在");
        }
        if (!stockCheck.getTenantId().equals(tenantId)) {
            throw new BusinessException(3202, "无权操作此盘点单");
        }
        if (stockCheck.getStatus() != 0 && stockCheck.getStatus() != 1) {
            throw new BusinessException(3203, "当前状态不允许完成盘点");
        }

        LambdaQueryWrapper<StockCheckItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(StockCheckItem::getCheckId, id);
        List<StockCheckItem> items = stockCheckItemMapper.selectList(itemWrapper);

        int totalActual = 0;
        int totalDiff = 0;

        for (StockCheckItem item : items) {
            if (item.getActualStock() == null) {
                item.setActualStock(0);
            }
            int diff = item.getActualStock() - item.getSystemStock();
            item.setDifference(diff);
            stockCheckItemMapper.updateById(item);

            totalActual += item.getActualStock();
            totalDiff += diff;

            if (diff != 0) {
                Stock stock = stockMapper.selectByProduct(tenantId, stockCheck.getStoreId(), item.getProductId());
                if (stock != null) {
                    BigDecimal oldWarehouseQty = stock.getWarehouseQuantity() != null ? stock.getWarehouseQuantity() : BigDecimal.ZERO;
                    stock.setWarehouseQuantity(BigDecimal.valueOf(item.getActualStock()));
                    stockMapper.updateById(stock);

                    StockLog stockLog = new StockLog();
                    stockLog.setTenantId(tenantId);
                    stockLog.setStoreId(stockCheck.getStoreId());
                    stockLog.setProductId(item.getProductId());
                    stockLog.setLogType(5);
                    stockLog.setQuantity(BigDecimal.valueOf(diff));
                    stockLog.setBeforeStock(oldWarehouseQty);
                    stockLog.setAfterStock(BigDecimal.valueOf(item.getActualStock()));
                    stockLog.setRelatedNo(stockCheck.getCheckNo());
                    stockLog.setRelatedId(stockCheck.getId());
                    stockLog.setOperatorId(userId);
                    stockLogMapper.insert(stockLog);
                }
            }
        }

        stockCheck.setTotalActual(totalActual);
        stockCheck.setTotalDiff(totalDiff);
        stockCheck.setStatus(2);
        stockCheckMapper.updateById(stockCheck);
    }
}

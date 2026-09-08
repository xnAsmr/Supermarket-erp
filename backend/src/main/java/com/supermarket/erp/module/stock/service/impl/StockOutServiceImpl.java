package com.supermarket.erp.module.stock.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.erp.common.exception.BusinessException;
import com.supermarket.erp.common.result.PageResult;
import com.supermarket.erp.module.auth.entity.User;
import com.supermarket.erp.module.auth.mapper.UserMapper;
import com.supermarket.erp.module.product.entity.Product;
import com.supermarket.erp.module.product.mapper.ProductMapper;
import com.supermarket.erp.module.stock.dto.StockOutCreateDTO;
import com.supermarket.erp.module.stock.entity.Stock;
import com.supermarket.erp.module.stock.entity.StockLog;
import com.supermarket.erp.module.stock.entity.StockOut;
import com.supermarket.erp.module.stock.entity.StockOutItem;
import com.supermarket.erp.module.stock.mapper.StockLogMapper;
import com.supermarket.erp.module.stock.mapper.StockMapper;
import com.supermarket.erp.module.stock.mapper.StockOutItemMapper;
import com.supermarket.erp.module.stock.mapper.StockOutMapper;
import com.supermarket.erp.module.stock.service.IStockOutService;
import com.supermarket.erp.module.stock.vo.StockOutVO;
import com.supermarket.erp.module.tenant.entity.Store;
import com.supermarket.erp.module.tenant.mapper.StoreMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockOutServiceImpl implements IStockOutService {

    private final StockOutMapper stockOutMapper;
    private final StockOutItemMapper stockOutItemMapper;
    private final StockMapper stockMapper;
    private final StockLogMapper stockLogMapper;
    private final ProductMapper productMapper;
    private final StoreMapper storeMapper;
    private final UserMapper userMapper;

    @Override
    public PageResult<StockOutVO> pageList(Integer page, Integer pageSize, Long tenantId) {
        LambdaQueryWrapper<StockOut> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockOut::getTenantId, tenantId);
        wrapper.orderByDesc(StockOut::getCreateTime);

        Page<StockOut> result = stockOutMapper.selectPage(new Page<>(page, pageSize), wrapper);

        List<StockOutVO> voList = result.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    @Override
    public StockOutVO getById(Long id) {
        StockOut stockOut = stockOutMapper.selectById(id);
        if (stockOut == null) {
            throw new BusinessException(3301, "出库单不存在");
        }
        return toVO(stockOut);
    }

    @Override
    public Long create(StockOutCreateDTO dto, Long tenantId, Long userId) {
        StockOut stockOut = new StockOut();
        stockOut.setTenantId(tenantId);
        stockOut.setOutNo("SO" + System.currentTimeMillis());
        stockOut.setOutType(dto.getOutType());
        stockOut.setStoreId(dto.getStoreId());
        stockOut.setTargetStoreId(dto.getTargetStoreId());
        stockOut.setStatus(0);
        stockOut.setOperatorId(userId);
        stockOut.setRemark(dto.getRemark());
        stockOutMapper.insert(stockOut);

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            int totalQuantity = 0;

            for (StockOutCreateDTO.StockOutItemDTO itemDTO : dto.getItems()) {
                StockOutItem item = new StockOutItem();
                item.setOutId(stockOut.getId());
                item.setProductId(itemDTO.getProductId());
                item.setQuantity(itemDTO.getQuantity());
                item.setRemark(itemDTO.getRemark());
                stockOutItemMapper.insert(item);
                totalQuantity += itemDTO.getQuantity();
            }

            stockOut.setTotalQuantity(totalQuantity);
            stockOutMapper.updateById(stockOut);
        }

        return stockOut.getId();
    }

    @Override
    public void update(Long id, StockOutCreateDTO dto, Long tenantId) {
        StockOut stockOut = stockOutMapper.selectById(id);
        if (stockOut == null) {
            throw new BusinessException(3301, "出库单不存在");
        }
        if (!stockOut.getTenantId().equals(tenantId)) {
            throw new BusinessException(3302, "无权操作此单据");
        }
        if (stockOut.getStatus() != 0) {
            throw new BusinessException(3303, "只有草稿状态可以编辑");
        }

        stockOut.setOutType(dto.getOutType());
        stockOut.setStoreId(dto.getStoreId());
        stockOut.setTargetStoreId(dto.getTargetStoreId());
        stockOut.setRemark(dto.getRemark());
        stockOutMapper.updateById(stockOut);

        stockOutItemMapper.deleteByOutId(id);

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            int totalQuantity = 0;

            for (StockOutCreateDTO.StockOutItemDTO itemDTO : dto.getItems()) {
                StockOutItem item = new StockOutItem();
                item.setOutId(id);
                item.setProductId(itemDTO.getProductId());
                item.setQuantity(itemDTO.getQuantity());
                item.setRemark(itemDTO.getRemark());
                stockOutItemMapper.insert(item);
                totalQuantity += itemDTO.getQuantity();
            }

            stockOut.setTotalQuantity(totalQuantity);
            stockOutMapper.updateById(stockOut);
        }
    }

    @Override
    public void delete(Long id, Long tenantId) {
        StockOut stockOut = stockOutMapper.selectById(id);
        if (stockOut == null) {
            throw new BusinessException(3301, "出库单不存在");
        }
        if (!stockOut.getTenantId().equals(tenantId)) {
            throw new BusinessException(3302, "无权操作此单据");
        }
        if (stockOut.getStatus() != 0) {
            throw new BusinessException(3303, "只有草稿状态可以删除");
        }
        stockOutMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void confirm(Long id, Long tenantId, Long userId) {
        StockOut stockOut = stockOutMapper.selectById(id);
        if (stockOut == null) {
            throw new BusinessException(3301, "出库单不存在");
        }
        if (!stockOut.getTenantId().equals(tenantId)) {
            throw new BusinessException(3302, "无权操作此单据");
        }
        if (stockOut.getStatus() != 0) {
            throw new BusinessException(3303, "当前状态不允许确认出库");
        }

        List<StockOutItem> items = stockOutItemMapper.selectByOutId(stockOut.getId());

        for (StockOutItem item : items) {
            Stock stock = stockMapper.selectByProduct(tenantId, stockOut.getStoreId(), item.getProductId());
            BigDecimal warehouseQty = stock != null && stock.getWarehouseQuantity() != null ? stock.getWarehouseQuantity() : BigDecimal.ZERO;
            if (warehouseQty.compareTo(BigDecimal.valueOf(item.getQuantity())) < 0) {
                Product product = productMapper.selectById(item.getProductId());
                String productName = product != null ? product.getName() : String.valueOf(item.getProductId());
                throw new BusinessException(3304, "仓库库存不足：" + productName + "（仓库" +
                        warehouseQty + "，需出" + item.getQuantity() + "）");
            }

            BigDecimal deductQty = BigDecimal.valueOf(item.getQuantity());
            BigDecimal newWarehouseQty = warehouseQty.subtract(deductQty);
            stock.setWarehouseQuantity(newWarehouseQty);

            // 销售出库：仓库→货架，shelf_quantity 增加
            if (stockOut.getOutType() != null && stockOut.getOutType() == 1) {
                BigDecimal shelfQty = stock.getShelfQuantity() != null ? stock.getShelfQuantity() : BigDecimal.ZERO;
                stock.setShelfQuantity(shelfQty.add(deductQty));
            }

            stockMapper.updateById(stock);

            Integer logType;
            switch (stockOut.getOutType()) {
                case 1: logType = 4; break;
                case 2: logType = 3; break;
                case 3: logType = 3; break;
                default: logType = 3;
            }

            StockLog stockLog = new StockLog();
            stockLog.setTenantId(tenantId);
            stockLog.setStoreId(stockOut.getStoreId());
            stockLog.setProductId(item.getProductId());
            stockLog.setLogType(logType);
            stockLog.setQuantity(deductQty);
            stockLog.setBeforeStock(warehouseQty);
            stockLog.setAfterStock(newWarehouseQty);
            stockLog.setRelatedNo(stockOut.getOutNo());
            stockLog.setRelatedId(stockOut.getId());
            stockLog.setOperatorId(userId);
            stockLogMapper.insert(stockLog);
        }

        stockOut.setStatus(1);
        stockOut.setStockOutUserId(userId);
        stockOut.setStockOutTime(LocalDateTime.now());
        stockOutMapper.updateById(stockOut);
    }

    @Override
    @Transactional
    public void rollbackConfirm(Long id, Long tenantId, Long userId) {
        StockOut stockOut = stockOutMapper.selectById(id);
        if (stockOut == null) {
            throw new BusinessException(3301, "出库单不存在");
        }
        if (!stockOut.getTenantId().equals(tenantId)) {
            throw new BusinessException(3302, "无权操作此单据");
        }
        if (stockOut.getStatus() != 1) {
            throw new BusinessException(3303, "只有已出库状态可以撤销");
        }

        List<StockOutItem> items = stockOutItemMapper.selectByOutId(stockOut.getId());

        for (StockOutItem item : items) {
            Stock stock = stockMapper.selectByProduct(tenantId, stockOut.getStoreId(), item.getProductId());
            if (stock != null) {
                BigDecimal restoreQty = BigDecimal.valueOf(item.getQuantity());
                BigDecimal oldWarehouseQty = stock.getWarehouseQuantity() != null ? stock.getWarehouseQuantity() : BigDecimal.ZERO;
                BigDecimal newWarehouseQty = oldWarehouseQty.add(restoreQty);
                stock.setWarehouseQuantity(newWarehouseQty);

                // 销售出库撤销：货架→仓库，shelf_quantity 减少
                if (stockOut.getOutType() != null && stockOut.getOutType() == 1) {
                    BigDecimal shelfQty = stock.getShelfQuantity() != null ? stock.getShelfQuantity() : BigDecimal.ZERO;
                    stock.setShelfQuantity(shelfQty.subtract(restoreQty));
                }

                stockMapper.updateById(stock);

                StockLog stockLog = new StockLog();
                stockLog.setTenantId(tenantId);
                stockLog.setStoreId(stockOut.getStoreId());
                stockLog.setProductId(item.getProductId());
                stockLog.setLogType(6);
                stockLog.setQuantity(restoreQty);
                stockLog.setBeforeStock(oldWarehouseQty);
                stockLog.setAfterStock(newWarehouseQty);
                stockLog.setRelatedNo(stockOut.getOutNo());
                stockLog.setRelatedId(stockOut.getId());
                stockLog.setOperatorId(userId);
                stockLogMapper.insert(stockLog);
            }
        }

        stockOut.setStatus(0);
        stockOut.setStockOutUserId(null);
        stockOut.setStockOutTime(null);
        stockOutMapper.updateById(stockOut);
    }

    private StockOutVO toVO(StockOut stockOut) {
        StockOutVO vo = new StockOutVO();
        BeanUtils.copyProperties(stockOut, vo);

        if (stockOut.getStoreId() != null) {
            Store store = storeMapper.selectById(stockOut.getStoreId());
            if (store != null) vo.setStoreName(store.getStoreName());
        }
        if (stockOut.getTargetStoreId() != null) {
            Store targetStore = storeMapper.selectById(stockOut.getTargetStoreId());
            if (targetStore != null) vo.setTargetStoreName(targetStore.getStoreName());
        }
        if (stockOut.getOperatorId() != null) {
            User operator = userMapper.selectById(stockOut.getOperatorId());
            if (operator != null) vo.setOperatorName(operator.getName());
        }
        if (stockOut.getStockOutUserId() != null) {
            User stockOutUser = userMapper.selectById(stockOut.getStockOutUserId());
            if (stockOutUser != null) vo.setStockOutUserName(stockOutUser.getName());
        }

        List<StockOutItem> items = stockOutItemMapper.selectByOutId(stockOut.getId());

        List<Long> productIds = items.stream().map(StockOutItem::getProductId).collect(Collectors.toList());
        Map<Long, Product> productMap = Map.of();
        if (!productIds.isEmpty()) {
            productMap = productMapper.selectBatchIds(productIds).stream()
                    .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));
        }

        Map<Long, Product> finalProductMap = productMap;
        List<StockOutVO.StockOutItemVO> itemVOs = items.stream()
                .map(item -> {
                    StockOutVO.StockOutItemVO itemVO = new StockOutVO.StockOutItemVO();
                    BeanUtils.copyProperties(item, itemVO);
                    Product product = finalProductMap.get(item.getProductId());
                    if (product != null) {
                        itemVO.setProductName(product.getName());
                    }
                    return itemVO;
                })
                .collect(Collectors.toList());
        vo.setItems(itemVOs);

        return vo;
    }
}

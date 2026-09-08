package com.supermarket.erp.module.stock.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.erp.common.exception.BusinessException;
import com.supermarket.erp.common.result.PageResult;
import com.supermarket.erp.module.product.entity.Product;
import com.supermarket.erp.module.product.mapper.ProductMapper;
import com.supermarket.erp.module.stock.dto.StockQueryDTO;
import com.supermarket.erp.module.stock.entity.Stock;
import com.supermarket.erp.module.stock.entity.StockLog;
import com.supermarket.erp.module.stock.entity.PurchaseIn;
import com.supermarket.erp.module.stock.entity.PurchaseInItem;
import com.supermarket.erp.module.stock.entity.StockOut;
import com.supermarket.erp.module.stock.entity.StockOutItem;
import com.supermarket.erp.module.stock.mapper.StockLogMapper;
import com.supermarket.erp.module.stock.mapper.StockMapper;
import com.supermarket.erp.module.stock.mapper.PurchaseInMapper;
import com.supermarket.erp.module.stock.mapper.PurchaseInItemMapper;
import com.supermarket.erp.module.stock.mapper.StockOutMapper;
import com.supermarket.erp.module.stock.mapper.StockOutItemMapper;
import com.supermarket.erp.module.stock.service.IStockService;
import com.supermarket.erp.module.stock.vo.StockDetailVO;
import com.supermarket.erp.module.stock.vo.StockVO;
import com.supermarket.erp.module.auth.entity.User;
import com.supermarket.erp.module.auth.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements IStockService {

    private final StockMapper stockMapper;
    private final StockLogMapper stockLogMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final PurchaseInMapper purchaseInMapper;
    private final PurchaseInItemMapper purchaseInItemMapper;
    private final StockOutMapper stockOutMapper;
    private final StockOutItemMapper stockOutItemMapper;

    @Override
    public PageResult<StockVO> pageList(StockQueryDTO query, Long tenantId) {
        LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.eq(Product::getTenantId, tenantId)
                .eq(Product::getDeleted, 0);

        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            String kw = query.getKeyword().trim();
            productWrapper.and(w -> w
                    .like(Product::getName, kw)
                    .or().like(Product::getBarcode, kw)
                    .or().like(Product::getPinyin, kw)
            );
        }
        if (query.getCategoryId() != null) {
            productWrapper.eq(Product::getCategoryId, query.getCategoryId());
        }

        productWrapper.orderByDesc(Product::getCreateTime);

        boolean needPostFilter = query.getStockStatus() != null && query.getStockStatus() != 0;

        Page<Product> productResult;
        if (needPostFilter) {
            List<Product> allProducts = productMapper.selectList(productWrapper);
            productResult = new Page<>(1, allProducts.size(), allProducts.size());
            productResult.setRecords(allProducts);
        } else {
            Page<Product> productPage = new Page<>(query.getPage(), query.getPageSize());
            productResult = productMapper.selectPage(productPage, productWrapper);
        }

        List<Long> productIds = productResult.getRecords().stream()
                .map(Product::getId)
                .collect(Collectors.toList());

        Map<Long, Stock> stockMap;
        if (productIds.isEmpty()) {
            stockMap = Map.of();
        } else {
            LambdaQueryWrapper<Stock> stockWrapper = new LambdaQueryWrapper<>();
            stockWrapper.eq(Stock::getTenantId, tenantId)
                    .in(Stock::getProductId, productIds);
            if (query.getStoreId() != null) {
                stockWrapper.eq(Stock::getStoreId, query.getStoreId());
            }
            stockMap = stockMapper.selectList(stockWrapper).stream()
                    .collect(Collectors.toMap(Stock::getProductId, s -> s, (a, b) -> a));
        }

        // 查询待入库数量：status 0(草稿) + 1(已审核) 的入库单明细
        Map<Long, BigDecimal> pendingInMap = calculatePendingInQuantity(tenantId, query.getStoreId(), productIds);

        // 查询待出库数量：status 0(待出库) 的出库单明细
        Map<Long, BigDecimal> pendingOutMap = calculatePendingOutQuantity(tenantId, query.getStoreId(), productIds);

        Map<Long, Stock> finalStockMap = stockMap;
        Map<Long, BigDecimal> finalPendingInMap = pendingInMap;
        Map<Long, BigDecimal> finalPendingOutMap = pendingOutMap;
        List<StockVO> voList = productResult.getRecords().stream()
                .map(product -> {
                    StockVO vo = new StockVO();
                    vo.setProductId(product.getId());
                    vo.setProductName(product.getName());
                    vo.setBarcode(product.getBarcode());

                    Stock stock = finalStockMap.get(product.getId());
                    if (stock != null) {
                        vo.setId(stock.getId());
                        vo.setTenantId(stock.getTenantId());
                        vo.setStoreId(stock.getStoreId());
                        vo.setWarehouseQuantity(stock.getWarehouseQuantity() != null ? stock.getWarehouseQuantity() : BigDecimal.ZERO);
                        vo.setShelfQuantity(stock.getShelfQuantity() != null ? stock.getShelfQuantity() : BigDecimal.ZERO);
                        vo.setAvgCost(stock.getAvgCost());
                    } else {
                        vo.setStoreId(query.getStoreId());
                        vo.setWarehouseQuantity(BigDecimal.ZERO);
                        vo.setShelfQuantity(BigDecimal.ZERO);
                    }
                    vo.setPendingInQuantity(finalPendingInMap.getOrDefault(product.getId(), BigDecimal.ZERO));
                    vo.setPendingOutQuantity(finalPendingOutMap.getOrDefault(product.getId(), BigDecimal.ZERO));
                    return vo;
                })
                .collect(Collectors.toList());

        if (needPostFilter) {
            voList = voList.stream()
                    .filter(vo -> query.getStockStatus() == 1
                            ? (vo.getShelfQuantity() != null && vo.getShelfQuantity().compareTo(BigDecimal.ZERO) > 0)
                            : (vo.getShelfQuantity() == null || vo.getShelfQuantity().compareTo(BigDecimal.ZERO) == 0))
                    .collect(Collectors.toList());

            int total = voList.size();
            int offset = (query.getPage() - 1) * query.getPageSize();
            int limit = Math.min(offset + query.getPageSize(), total);
            List<StockVO> pageList = offset < total ? voList.subList(offset, limit) : List.of();
            return new PageResult<>(pageList, (long) total, query.getPage(), query.getPageSize());
        }

        return new PageResult<>(voList, productResult.getTotal(), (int) productResult.getCurrent(), (int) productResult.getSize());
    }

    /**
     * 计算待入库数量：status 0(草稿) + 1(已审核) 的入库单明细数量之和
     */
    private Map<Long, BigDecimal> calculatePendingInQuantity(Long tenantId, Long storeId, List<Long> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }

        // 查询 status 0 或 1 的入库单
        LambdaQueryWrapper<PurchaseIn> inWrapper = new LambdaQueryWrapper<>();
        inWrapper.eq(PurchaseIn::getTenantId, tenantId)
                .in(PurchaseIn::getStatus, List.of(0, 1))
                .eq(PurchaseIn::getDeleted, 0);
        if (storeId != null) {
            inWrapper.eq(PurchaseIn::getStoreId, storeId);
        }
        List<PurchaseIn> pendingIns = purchaseInMapper.selectList(inWrapper);
        if (pendingIns.isEmpty()) {
            return Map.of();
        }

        List<Long> inIds = pendingIns.stream().map(PurchaseIn::getId).collect(Collectors.toList());

        // 查询这些入库单的明细
        LambdaQueryWrapper<PurchaseInItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.in(PurchaseInItem::getInId, inIds)
                .in(PurchaseInItem::getProductId, productIds);
        List<PurchaseInItem> items = purchaseInItemMapper.selectList(itemWrapper);

        // 按 productId 汇总数量
        return items.stream()
                .collect(Collectors.groupingBy(
                        PurchaseInItem::getProductId,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                item -> BigDecimal.valueOf(item.getQuantity()),
                                BigDecimal::add
                        )
                ));
    }

    /**
     * 计算待出库数量：status 0(待出库) 的出库单明细数量之和
     */
    private Map<Long, BigDecimal> calculatePendingOutQuantity(Long tenantId, Long storeId, List<Long> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }

        // 查询 status 0 的出库单
        LambdaQueryWrapper<StockOut> outWrapper = new LambdaQueryWrapper<>();
        outWrapper.eq(StockOut::getTenantId, tenantId)
                .eq(StockOut::getStatus, 0)
                .eq(StockOut::getDeleted, 0);
        if (storeId != null) {
            outWrapper.eq(StockOut::getStoreId, storeId);
        }
        List<StockOut> pendingOuts = stockOutMapper.selectList(outWrapper);
        if (pendingOuts.isEmpty()) {
            return Map.of();
        }

        List<Long> outIds = pendingOuts.stream().map(StockOut::getId).collect(Collectors.toList());

        // 查询这些出库单的明细
        LambdaQueryWrapper<StockOutItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.in(StockOutItem::getOutId, outIds)
                .in(StockOutItem::getProductId, productIds);
        List<StockOutItem> items = stockOutItemMapper.selectList(itemWrapper);

        // 按 productId 汇总数量
        return items.stream()
                .collect(Collectors.groupingBy(
                        StockOutItem::getProductId,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                item -> BigDecimal.valueOf(item.getQuantity()),
                                BigDecimal::add
                        )
                ));
    }

    @Override
    public StockDetailVO getDetail(Long tenantId, Long productId, Long storeId) {
        StockDetailVO vo = new StockDetailVO();
        vo.setProductId(productId);

        Product product = productMapper.selectById(productId);
        if (product != null) {
            vo.setProductName(product.getName());
            vo.setBarcode(product.getBarcode());
            vo.setSpec(product.getSpec());
            vo.setUnit(product.getUnit());
        }

        LambdaQueryWrapper<Stock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Stock::getTenantId, tenantId)
                .eq(Stock::getProductId, productId);
        if (storeId != null) {
            wrapper.eq(Stock::getStoreId, storeId);
        }
        Stock stock = stockMapper.selectOne(wrapper);
        if (stock != null) {
            vo.setStoreId(stock.getStoreId());
            vo.setWarehouseQuantity(stock.getWarehouseQuantity() != null ? stock.getWarehouseQuantity() : BigDecimal.ZERO);
            vo.setShelfQuantity(stock.getShelfQuantity() != null ? stock.getShelfQuantity() : BigDecimal.ZERO);
            vo.setAvgCost(stock.getAvgCost());
        }

        // 计算待入库数量
        Map<Long, BigDecimal> pendingInMap = calculatePendingInQuantity(tenantId, storeId, List.of(productId));
        vo.setPendingInQuantity(pendingInMap.getOrDefault(productId, BigDecimal.ZERO));

        // 计算待出库数量
        Map<Long, BigDecimal> pendingOutMap = calculatePendingOutQuantity(tenantId, storeId, List.of(productId));
        vo.setPendingOutQuantity(pendingOutMap.getOrDefault(productId, BigDecimal.ZERO));

        LambdaQueryWrapper<StockLog> logWrapper = new LambdaQueryWrapper<>();
        logWrapper.eq(StockLog::getTenantId, tenantId)
                .eq(StockLog::getProductId, productId);
        if (storeId != null) {
            logWrapper.eq(StockLog::getStoreId, storeId);
        }
        logWrapper.orderByDesc(StockLog::getCreateTime);
        logWrapper.last("LIMIT 50");

        List<StockLog> logs = stockLogMapper.selectList(logWrapper);
        List<StockDetailVO.StockLogVO> logVOs = logs.stream().map(log -> {
            StockDetailVO.StockLogVO logVO = new StockDetailVO.StockLogVO();
            logVO.setId(log.getId());
            logVO.setLogType(log.getLogType());
            logVO.setLogTypeName(getLogTypeName(log.getLogType()));
            logVO.setQuantity(log.getQuantity());
            logVO.setBeforeStock(log.getBeforeStock());
            logVO.setAfterStock(log.getAfterStock());
            logVO.setRelatedNo(log.getRelatedNo());
            logVO.setCreateTime(log.getCreateTime());
            if (log.getOperatorId() != null) {
                User operator = userMapper.selectById(log.getOperatorId());
                if (operator != null) logVO.setOperatorName(operator.getName());
            }
            return logVO;
        }).collect(Collectors.toList());
        vo.setLogs(logVOs);

        return vo;
    }

    private String getLogTypeName(Integer logType) {
        if (logType == null) return "未知";
        switch (logType) {
            case 1: return "采购入库";
            case 2: return "手动入库";
            case 3: return "手动出库";
            case 4: return "销售出库";
            case 5: return "盘点调整";
            case 6: return "入库回退";
            default: return "其他";
        }
    }

    @Override
    public StockVO getStockByProduct(Long tenantId, Long storeId, Long productId) {
        Stock stock = stockMapper.selectByProduct(tenantId, storeId, productId);
        if (stock == null) {
            throw new BusinessException(3001, "库存记录不存在");
        }
        StockVO vo = new StockVO();
        BeanUtils.copyProperties(stock, vo);

        Product product = productMapper.selectById(productId);
        if (product != null) {
            vo.setProductName(product.getName());
            vo.setBarcode(product.getBarcode());
        }
        return vo;
    }

    @Override
    @Transactional
    public void adjustStock(Long tenantId, Long storeId, Long productId, Integer quantity, String type, Long userId) {
        if (quantity == null || quantity <= 0) {
            throw new BusinessException(3201, "数量必须大于0");
        }

        Stock stock = stockMapper.selectByProduct(tenantId, storeId, productId);
        BigDecimal oldWarehouseQty = stock != null && stock.getWarehouseQuantity() != null ? stock.getWarehouseQuantity() : BigDecimal.ZERO;
        BigDecimal newWarehouseQty;

        if ("IN".equals(type)) {
            newWarehouseQty = oldWarehouseQty.add(BigDecimal.valueOf(quantity));
        } else {
            if (stock == null || oldWarehouseQty.compareTo(BigDecimal.valueOf(quantity)) < 0) {
                throw new BusinessException(3202, "仓库库存不足");
            }
            newWarehouseQty = oldWarehouseQty.subtract(BigDecimal.valueOf(quantity));
        }

        if (stock != null) {
            stock.setWarehouseQuantity(newWarehouseQty);
            stockMapper.updateById(stock);
        } else {
            if ("OUT".equals(type)) {
                throw new BusinessException(3203, "库存记录不存在");
            }
            stock = new Stock();
            stock.setTenantId(tenantId);
            stock.setStoreId(storeId);
            stock.setProductId(productId);
            stock.setWarehouseQuantity(BigDecimal.valueOf(quantity));
            stock.setShelfQuantity(BigDecimal.ZERO);
            stock.setAvgCost(BigDecimal.ZERO);
            stockMapper.insert(stock);
            newWarehouseQty = BigDecimal.valueOf(quantity);
        }

        StockLog stockLog = new StockLog();
        stockLog.setTenantId(tenantId);
        stockLog.setStoreId(storeId);
        stockLog.setProductId(productId);
        stockLog.setLogType("IN".equals(type) ? 2 : 3);
        stockLog.setQuantity(BigDecimal.valueOf(quantity));
        stockLog.setBeforeStock(oldWarehouseQty);
        stockLog.setAfterStock(newWarehouseQty);
        stockLog.setOperatorId(userId);
        stockLogMapper.insert(stockLog);
    }
}

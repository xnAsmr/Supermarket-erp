package com.supermarket.erp.module.stock.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.erp.common.exception.BusinessException;
import com.supermarket.erp.module.stock.dto.PurchaseInCreateDTO;
import com.supermarket.erp.module.stock.entity.PurchaseIn;
import com.supermarket.erp.module.stock.entity.PurchaseInItem;
import com.supermarket.erp.module.stock.entity.Stock;
import com.supermarket.erp.module.stock.entity.StockLog;
import com.supermarket.erp.module.stock.mapper.PurchaseInItemMapper;
import com.supermarket.erp.module.stock.mapper.PurchaseInMapper;
import com.supermarket.erp.module.stock.mapper.StockLogMapper;
import com.supermarket.erp.module.stock.mapper.StockMapper;
import com.supermarket.erp.module.stock.service.IPurchaseInService;
import com.supermarket.erp.module.stock.vo.PurchaseInVO;
import com.supermarket.erp.module.supplier.entity.Supplier;
import com.supermarket.erp.module.supplier.mapper.SupplierMapper;
import com.supermarket.erp.module.tenant.entity.Store;
import com.supermarket.erp.module.tenant.mapper.StoreMapper;
import com.supermarket.erp.module.auth.entity.User;
import com.supermarket.erp.module.auth.mapper.UserMapper;
import com.supermarket.erp.module.product.entity.Product;
import com.supermarket.erp.module.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseInServiceImpl implements IPurchaseInService {

    private final PurchaseInMapper purchaseInMapper;
    private final PurchaseInItemMapper purchaseInItemMapper;
    private final StockMapper stockMapper;
    private final StockLogMapper stockLogMapper;
    private final SupplierMapper supplierMapper;
    private final StoreMapper storeMapper;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;

    @Override
    public PurchaseInVO getById(Long id) {
        PurchaseIn purchaseIn = purchaseInMapper.selectById(id);
        if (purchaseIn == null) {
            throw new BusinessException(3101, "采购入库单不存在");
        }
        return toVO(purchaseIn);
    }

    @Override
    public Page<PurchaseInVO> pageList(Integer page, Integer pageSize, Long tenantId) {
        LambdaQueryWrapper<PurchaseIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseIn::getTenantId, tenantId);
        wrapper.orderByDesc(PurchaseIn::getCreateTime);

        Page<PurchaseIn> result = purchaseInMapper.selectPage(new Page<>(page, pageSize), wrapper);

        Page<PurchaseInVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<PurchaseInVO> voList = result.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public Long create(PurchaseInCreateDTO dto, Long tenantId, Long userId) {
        PurchaseIn purchaseIn = new PurchaseIn();
        purchaseIn.setTenantId(tenantId);
        purchaseIn.setInNo("PI" + System.currentTimeMillis());
        purchaseIn.setSupplierId(dto.getSupplierId());
        purchaseIn.setStoreId(dto.getStoreId());
        purchaseIn.setStatus(0);
        purchaseIn.setOperatorId(userId);
        purchaseInMapper.insert(purchaseIn);

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            int totalQuantity = 0;
            BigDecimal totalAmount = BigDecimal.ZERO;

            for (PurchaseInCreateDTO.PurchaseInItemDTO itemDTO : dto.getItems()) {
                PurchaseInItem item = new PurchaseInItem();
                item.setInId(purchaseIn.getId());
                item.setProductId(itemDTO.getProductId());
                item.setQuantity(itemDTO.getQuantity());
                item.setPurchasePrice(itemDTO.getPurchasePrice());
                item.setTotalPrice(itemDTO.getPurchasePrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
                item.setBatchNo(itemDTO.getBatchNo());
                item.setExpireTime(itemDTO.getExpireTime());
                purchaseInItemMapper.insert(item);

                totalQuantity += itemDTO.getQuantity();
                totalAmount = totalAmount.add(item.getTotalPrice());
            }

            purchaseIn.setTotalQuantity(totalQuantity);
            purchaseIn.setTotalAmount(totalAmount);
            purchaseInMapper.updateById(purchaseIn);
        }

        return purchaseIn.getId();
    }

    @Override
    public void review(Long id, Long tenantId, Long userId) {
        PurchaseIn purchaseIn = purchaseInMapper.selectById(id);
        if (purchaseIn == null) {
            throw new BusinessException(3101, "采购入库单不存在");
        }
        if (!purchaseIn.getTenantId().equals(tenantId)) {
            throw new BusinessException(3102, "无权操作此单据");
        }
        if (purchaseIn.getStatus() != 0) {
            throw new BusinessException(3103, "当前状态不允许审核");
        }
        purchaseIn.setStatus(1);
        purchaseIn.setReviewerId(userId);
        purchaseIn.setReviewTime(LocalDateTime.now());
        purchaseInMapper.updateById(purchaseIn);
    }

    @Override
    public void delete(Long id, Long tenantId) {
        PurchaseIn purchaseIn = purchaseInMapper.selectById(id);
        if (purchaseIn == null) {
            throw new BusinessException(3101, "采购入库单不存在");
        }
        if (!purchaseIn.getTenantId().equals(tenantId)) {
            throw new BusinessException(3102, "无权操作此单据");
        }
        if (purchaseIn.getStatus() != 0) {
            throw new BusinessException(3103, "只有草稿状态可以删除");
        }
        purchaseInMapper.deleteById(id);
    }

    @Override
    public void revokeReview(Long id, Long tenantId) {
        PurchaseIn purchaseIn = purchaseInMapper.selectById(id);
        if (purchaseIn == null) {
            throw new BusinessException(3101, "采购入库单不存在");
        }
        if (!purchaseIn.getTenantId().equals(tenantId)) {
            throw new BusinessException(3102, "无权操作此单据");
        }
        if (purchaseIn.getStatus() != 1) {
            throw new BusinessException(3103, "只有已审核状态可以撤销");
        }
        purchaseIn.setStatus(0);
        purchaseIn.setReviewerId(null);
        purchaseIn.setReviewTime(null);
        purchaseIn.setReviewRemark(null);
        purchaseInMapper.updateById(purchaseIn);
    }

    @Override
    @Transactional
    public void rollbackStockIn(Long id, Long tenantId, Long userId) {
        PurchaseIn purchaseIn = purchaseInMapper.selectById(id);
        if (purchaseIn == null) {
            throw new BusinessException(3101, "采购入库单不存在");
        }
        if (!purchaseIn.getTenantId().equals(tenantId)) {
            throw new BusinessException(3102, "无权操作此单据");
        }
        if (purchaseIn.getStatus() != 2) {
            throw new BusinessException(3103, "只有已入库状态可以回退");
        }

        List<PurchaseInItem> items = purchaseInItemMapper.selectByInId(purchaseIn.getId());
        for (PurchaseInItem item : items) {
            Stock stock = stockMapper.selectByProduct(tenantId, purchaseIn.getStoreId(), item.getProductId());
            if (stock != null) {
                BigDecimal oldWarehouseQty = stock.getWarehouseQuantity() != null ? stock.getWarehouseQuantity() : BigDecimal.ZERO;
                BigDecimal newWarehouseQty = oldWarehouseQty.subtract(BigDecimal.valueOf(item.getQuantity()));
                if (newWarehouseQty.compareTo(BigDecimal.ZERO) < 0) {
                    throw new BusinessException(3104, "仓库库存不足，无法回退（商品：" + item.getProductId() + "）");
                }
                stock.setWarehouseQuantity(newWarehouseQty);
                stockMapper.updateById(stock);

                StockLog stockLog = new StockLog();
                stockLog.setTenantId(tenantId);
                stockLog.setStoreId(purchaseIn.getStoreId());
                stockLog.setProductId(item.getProductId());
                stockLog.setLogType(6);
                stockLog.setQuantity(BigDecimal.valueOf(item.getQuantity()));
                stockLog.setBeforeStock(oldWarehouseQty);
                stockLog.setAfterStock(newWarehouseQty);
                stockLog.setRelatedNo(purchaseIn.getInNo());
                stockLog.setRelatedId(purchaseIn.getId());
                stockLog.setOperatorId(userId);
                stockLogMapper.insert(stockLog);
            }
        }

        purchaseIn.setStatus(1);
        purchaseIn.setStockInUserId(null);
        purchaseIn.setStockInTime(null);
        purchaseInMapper.updateById(purchaseIn);
    }

    @Override
    @Transactional
    public void update(Long id, PurchaseInCreateDTO dto, Long tenantId) {
        PurchaseIn purchaseIn = purchaseInMapper.selectById(id);
        if (purchaseIn == null) {
            throw new BusinessException(3101, "采购入库单不存在");
        }
        if (!purchaseIn.getTenantId().equals(tenantId)) {
            throw new BusinessException(3102, "无权操作此单据");
        }
        if (purchaseIn.getStatus() != 0) {
            throw new BusinessException(3103, "只有草稿状态可以编辑");
        }

        purchaseIn.setSupplierId(dto.getSupplierId());
        purchaseIn.setStoreId(dto.getStoreId());
        purchaseInMapper.updateById(purchaseIn);

        purchaseInItemMapper.deleteByInId(id);

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            BigDecimal totalAmount = BigDecimal.ZERO;
            int totalQuantity = 0;

            for (PurchaseInCreateDTO.PurchaseInItemDTO itemDTO : dto.getItems()) {
                PurchaseInItem item = new PurchaseInItem();
                item.setInId(id);
                item.setProductId(itemDTO.getProductId());
                item.setQuantity(itemDTO.getQuantity());
                item.setPurchasePrice(itemDTO.getPurchasePrice());
                item.setTotalPrice(itemDTO.getPurchasePrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
                item.setBatchNo(itemDTO.getBatchNo());
                item.setExpireTime(itemDTO.getExpireTime());
                purchaseInItemMapper.insert(item);

                totalAmount = totalAmount.add(item.getTotalPrice());
                totalQuantity += item.getQuantity();
            }

            purchaseIn.setTotalQuantity(totalQuantity);
            purchaseIn.setTotalAmount(totalAmount);
            purchaseInMapper.updateById(purchaseIn);
        }
    }

    @Override
    @Transactional
    public void stockIn(Long id, Long tenantId, Long userId) {
        PurchaseIn purchaseIn = purchaseInMapper.selectById(id);
        if (purchaseIn == null) {
            throw new BusinessException(3101, "采购入库单不存在");
        }
        if (!purchaseIn.getTenantId().equals(tenantId)) {
            throw new BusinessException(3102, "无权操作此单据");
        }
        if (purchaseIn.getStatus() != 1) {
            throw new BusinessException(3103, "当前状态不允许入库");
        }

        List<PurchaseInItem> items = purchaseInItemMapper.selectByInId(id);

        for (PurchaseInItem item : items) {
            Stock stock = stockMapper.selectByProduct(tenantId, purchaseIn.getStoreId(), item.getProductId());

            if (stock != null) {
                BigDecimal oldWarehouseQty = stock.getWarehouseQuantity() != null ? stock.getWarehouseQuantity() : BigDecimal.ZERO;
                BigDecimal newWarehouseQty = oldWarehouseQty.add(BigDecimal.valueOf(item.getQuantity()));
                stock.setWarehouseQuantity(newWarehouseQty);

                BigDecimal totalCost = stock.getAvgCost().multiply(oldWarehouseQty)
                        .add(item.getPurchasePrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                stock.setAvgCost(totalCost.divide(newWarehouseQty, 2, RoundingMode.HALF_UP));
                stockMapper.updateById(stock);

                StockLog stockLog = new StockLog();
                stockLog.setTenantId(tenantId);
                stockLog.setStoreId(purchaseIn.getStoreId());
                stockLog.setProductId(item.getProductId());
                stockLog.setLogType(1);
                stockLog.setQuantity(BigDecimal.valueOf(item.getQuantity()));
                stockLog.setBeforeStock(oldWarehouseQty);
                stockLog.setAfterStock(newWarehouseQty);
                stockLog.setRelatedNo(purchaseIn.getInNo());
                stockLog.setRelatedId(purchaseIn.getId());
                stockLog.setOperatorId(userId);
                stockLogMapper.insert(stockLog);
            } else {
                Stock newStock = new Stock();
                newStock.setTenantId(tenantId);
                newStock.setStoreId(purchaseIn.getStoreId());
                newStock.setProductId(item.getProductId());
                newStock.setWarehouseQuantity(BigDecimal.valueOf(item.getQuantity()));
                newStock.setShelfQuantity(BigDecimal.ZERO);
                newStock.setAvgCost(item.getPurchasePrice());
                stockMapper.insert(newStock);

                StockLog stockLog = new StockLog();
                stockLog.setTenantId(tenantId);
                stockLog.setStoreId(purchaseIn.getStoreId());
                stockLog.setProductId(item.getProductId());
                stockLog.setLogType(1);
                stockLog.setQuantity(BigDecimal.valueOf(item.getQuantity()));
                stockLog.setBeforeStock(BigDecimal.ZERO);
                stockLog.setAfterStock(BigDecimal.valueOf(item.getQuantity()));
                stockLog.setRelatedNo(purchaseIn.getInNo());
                stockLog.setRelatedId(purchaseIn.getId());
                stockLog.setOperatorId(userId);
                stockLogMapper.insert(stockLog);
            }
        }

        purchaseIn.setStatus(2);
        purchaseIn.setStockInUserId(userId);
        purchaseIn.setStockInTime(LocalDateTime.now());
        purchaseInMapper.updateById(purchaseIn);
    }

    private PurchaseInVO toVO(PurchaseIn purchaseIn) {
        PurchaseInVO vo = new PurchaseInVO();
        BeanUtils.copyProperties(purchaseIn, vo);

        if (purchaseIn.getSupplierId() != null) {
            Supplier supplier = supplierMapper.selectById(purchaseIn.getSupplierId());
            if (supplier != null) vo.setSupplierName(supplier.getName());
        }
        if (purchaseIn.getStoreId() != null) {
            Store store = storeMapper.selectById(purchaseIn.getStoreId());
            if (store != null) vo.setStoreName(store.getStoreName());
        }
        if (purchaseIn.getOperatorId() != null) {
            User operator = userMapper.selectById(purchaseIn.getOperatorId());
            if (operator != null) vo.setOperatorName(operator.getName());
        }
        if (purchaseIn.getReviewerId() != null) {
            User reviewer = userMapper.selectById(purchaseIn.getReviewerId());
            if (reviewer != null) vo.setReviewerName(reviewer.getName());
        }
        if (purchaseIn.getStockInUserId() != null) {
            User stockInUser = userMapper.selectById(purchaseIn.getStockInUserId());
            if (stockInUser != null) vo.setStockInUserName(stockInUser.getName());
        }

        List<PurchaseInItem> items = purchaseInItemMapper.selectByInId(purchaseIn.getId());

        List<Long> productIds = items.stream().map(PurchaseInItem::getProductId).collect(Collectors.toList());
        Map<Long, Product> productMap = Map.of();
        if (!productIds.isEmpty()) {
            productMap = productMapper.selectBatchIds(productIds).stream()
                    .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));
        }

        Map<Long, Product> finalProductMap = productMap;
        List<PurchaseInVO.PurchaseInItemVO> itemVOs = items.stream()
                .map(item -> {
                    PurchaseInVO.PurchaseInItemVO itemVO = new PurchaseInVO.PurchaseInItemVO();
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

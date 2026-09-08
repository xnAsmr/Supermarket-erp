package com.supermarket.erp.module.supplier.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.erp.common.exception.BusinessException;
import com.supermarket.erp.common.result.PageResult;
import com.supermarket.erp.module.supplier.dto.SupplierCreateDTO;
import com.supermarket.erp.module.supplier.dto.SupplierQueryDTO;
import com.supermarket.erp.module.supplier.entity.Supplier;
import com.supermarket.erp.module.supplier.mapper.SupplierMapper;
import com.supermarket.erp.module.supplier.service.ISupplierService;
import com.supermarket.erp.module.supplier.vo.SupplierVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements ISupplierService {

    private final SupplierMapper supplierMapper;

    @Override
    public SupplierVO getById(Long id) {
        Supplier supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            throw new BusinessException(6001, "供应商不存在");
        }
        return toVO(supplier);
    }

    @Override
    public PageResult<SupplierVO> pageList(SupplierQueryDTO query) {
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Supplier::getDeleted, 0);

        if (query.getTenantId() != null) {
            wrapper.eq(Supplier::getTenantId, query.getTenantId());
        }

        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            String kw = query.getKeyword().trim();
            wrapper.and(w -> w
                    .like(Supplier::getName, kw)
                    .or().like(Supplier::getContact, kw)
            );
        }
        if (query.getStatus() != null) {
            wrapper.eq(Supplier::getStatus, query.getStatus());
        }

        wrapper.orderByDesc(Supplier::getCreateTime);

        Page<Supplier> page = new Page<>(query.getPage(), query.getPageSize());
        Page<Supplier> result = supplierMapper.selectPage(page, wrapper);

        List<SupplierVO> voList = result.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    @Override
    public Long create(SupplierCreateDTO dto, Long tenantId, Long userId) {
        Supplier supplier = new Supplier();
        BeanUtils.copyProperties(dto, supplier);
        supplier.setTenantId(tenantId);
        supplier.setSupplierNo("S" + System.currentTimeMillis());
        supplier.setStatus(1);
        supplierMapper.insert(supplier);
        return supplier.getId();
    }

    @Override
    public void update(Long id, SupplierCreateDTO dto, Long tenantId, Long userId) {
        Supplier supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            throw new BusinessException(6001, "供应商不存在");
        }

        BeanUtils.copyProperties(dto, supplier);
        supplierMapper.updateById(supplier);
    }

    @Override
    public void delete(Long id, Long tenantId) {
        Supplier supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            throw new BusinessException(6001, "供应商不存在");
        }
        supplierMapper.deleteById(id);
    }

    private SupplierVO toVO(Supplier supplier) {
        SupplierVO vo = new SupplierVO();
        BeanUtils.copyProperties(supplier, vo);
        return vo;
    }
}

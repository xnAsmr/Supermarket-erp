package com.supermarket.erp.module.tenant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.erp.common.exception.BusinessException;
import com.supermarket.erp.common.result.PageResult;
import com.supermarket.erp.module.tenant.dto.StoreCreateDTO;
import com.supermarket.erp.module.tenant.entity.Store;
import com.supermarket.erp.module.tenant.mapper.StoreMapper;
import com.supermarket.erp.module.tenant.service.IStoreService;
import com.supermarket.erp.module.tenant.vo.StoreVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements IStoreService {

    private final StoreMapper storeMapper;

    @Override
    public PageResult<StoreVO> pageList(Long tenantId, Integer page, Integer pageSize) {
        LambdaQueryWrapper<Store> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Store::getDeleted, 0);
        if (tenantId != null) {
            wrapper.eq(Store::getTenantId, tenantId);
        }
        wrapper.orderByDesc(Store::getCreateTime);

        Page<Store> storePage = new Page<>(page, pageSize);
        Page<Store> result = storeMapper.selectPage(storePage, wrapper);

        List<StoreVO> voList = result.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    @Override
    public StoreVO getById(Long id) {
        Store store = storeMapper.selectById(id);
        if (store == null) {
            throw new BusinessException(404, "门店不存在");
        }
        return toVO(store);
    }

    @Override
    public Long create(StoreCreateDTO dto, Long tenantId) {
        Store store = new Store();
        store.setStoreName(dto.getStoreName());
        store.setAddress(dto.getAddress());
        store.setContactPhone(dto.getContactPhone());
        store.setManagerId(dto.getManagerId());
        store.setStoreType(dto.getStoreType());
        store.setBusinessHours(dto.getBusinessHours());
        store.setLogo(dto.getLogo());
        store.setSort(dto.getSort());
        store.setRemark(dto.getRemark());
        store.setTenantId(tenantId);
        store.setStoreNo("ST" + System.currentTimeMillis());
        store.setStatus(1);
        storeMapper.insert(store);
        return store.getId();
    }

    @Override
    public void update(Long id, StoreCreateDTO dto) {
        Store store = storeMapper.selectById(id);
        if (store == null) {
            throw new BusinessException(404, "门店不存在");
        }
        store.setStoreName(dto.getStoreName());
        store.setAddress(dto.getAddress());
        store.setContactPhone(dto.getContactPhone());
        store.setManagerId(dto.getManagerId());
        store.setStoreType(dto.getStoreType());
        store.setBusinessHours(dto.getBusinessHours());
        store.setLogo(dto.getLogo());
        store.setSort(dto.getSort());
        store.setRemark(dto.getRemark());
        storeMapper.updateById(store);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Store store = storeMapper.selectById(id);
        if (store == null) {
            throw new BusinessException(404, "门店不存在");
        }
        store.setStatus(status);
        storeMapper.updateById(store);
    }

    @Override
    public void delete(Long id) {
        Store store = storeMapper.selectById(id);
        if (store == null) {
            throw new BusinessException(404, "门店不存在");
        }
        storeMapper.deleteById(id);
    }

    @Override
    public long countByTenantId(Long tenantId) {
        LambdaQueryWrapper<Store> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Store::getDeleted, 0)
               .eq(Store::getTenantId, tenantId);
        return storeMapper.selectCount(wrapper);
    }

    private StoreVO toVO(Store store) {
        StoreVO vo = new StoreVO();
        BeanUtils.copyProperties(store, vo);
        return vo;
    }
}

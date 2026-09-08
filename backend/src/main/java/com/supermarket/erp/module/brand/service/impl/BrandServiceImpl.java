package com.supermarket.erp.module.brand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.erp.common.exception.BusinessException;
import com.supermarket.erp.common.result.PageResult;
import com.supermarket.erp.module.brand.dto.BrandCreateDTO;
import com.supermarket.erp.module.brand.dto.BrandQueryDTO;
import com.supermarket.erp.module.brand.entity.Brand;
import com.supermarket.erp.module.brand.mapper.BrandMapper;
import com.supermarket.erp.module.brand.service.IBrandService;
import com.supermarket.erp.module.brand.vo.BrandVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements IBrandService {

    private final BrandMapper brandMapper;

    @Override
    public BrandVO getById(Long id) {
        Brand brand = brandMapper.selectById(id);
        if (brand == null) {
            throw new BusinessException(6001, "品牌不存在");
        }
        return toVO(brand);
    }

    @Override
    public PageResult<BrandVO> pageList(BrandQueryDTO query) {
        LambdaQueryWrapper<Brand> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Brand::getDeleted, 0);

        if (query.getTenantId() != null) {
            wrapper.eq(Brand::getTenantId, query.getTenantId());
        }

        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            String kw = query.getKeyword().trim();
            wrapper.like(Brand::getName, kw);
        }
        if (query.getStatus() != null) {
            wrapper.eq(Brand::getStatus, query.getStatus());
        }

        wrapper.orderByAsc(Brand::getSort).orderByDesc(Brand::getCreateTime);

        Page<Brand> page = new Page<>(query.getPage(), query.getPageSize());
        Page<Brand> result = brandMapper.selectPage(page, wrapper);

        List<BrandVO> voList = result.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    @Override
    public Long create(BrandCreateDTO dto, Long tenantId, Long userId) {
        Brand brand = new Brand();
        BeanUtils.copyProperties(dto, brand);
        brand.setTenantId(tenantId);
        brand.setStatus(1);
        brand.setCreateBy(userId);
        brand.setUpdateBy(userId);
        brandMapper.insert(brand);
        return brand.getId();
    }

    @Override
    public void update(Long id, BrandCreateDTO dto, Long tenantId, Long userId) {
        Brand brand = brandMapper.selectById(id);
        if (brand == null) {
            throw new BusinessException(6001, "品牌不存在");
        }

        BeanUtils.copyProperties(dto, brand);
        brand.setUpdateBy(userId);
        brandMapper.updateById(brand);
    }

    @Override
    public void delete(Long id, Long tenantId) {
        Brand brand = brandMapper.selectById(id);
        if (brand == null) {
            throw new BusinessException(6001, "品牌不存在");
        }
        brandMapper.deleteById(id);
    }

    private BrandVO toVO(Brand brand) {
        BrandVO vo = new BrandVO();
        BeanUtils.copyProperties(brand, vo);
        return vo;
    }
}

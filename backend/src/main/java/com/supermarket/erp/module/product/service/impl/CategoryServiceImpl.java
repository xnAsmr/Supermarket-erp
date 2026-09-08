package com.supermarket.erp.module.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supermarket.erp.common.exception.BusinessException;
import com.supermarket.erp.module.product.entity.Category;
import com.supermarket.erp.module.product.mapper.CategoryMapper;
import com.supermarket.erp.module.product.service.ICategoryService;
import com.supermarket.erp.module.product.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements ICategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryVO> getTree(Long tenantId) {
        List<Category> roots = categoryMapper.selectRootCategories(tenantId);
        return roots.stream().map(root -> buildTree(root, tenantId)).collect(Collectors.toList());
    }

    @Override
    public List<CategoryVO> getChildren(Long parentId, Long tenantId) {
        List<Category> children = categoryMapper.selectByParentId(parentId, tenantId);
        return children.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public Long create(String name, String code, Long parentId, Long tenantId) {
        Category category = new Category();
        category.setName(name);
        category.setCode(code);
        category.setParentId(parentId);
        category.setTenantId(tenantId);
        category.setStatus(1);

        if (parentId == null || parentId == 0) {
            category.setLevel(1);
            category.setParentId(0L);
        } else {
            Category parent = categoryMapper.selectById(parentId);
            if (parent == null) {
                throw new BusinessException("父分类不存在");
            }
            category.setLevel(parent.getLevel() + 1);
        }

        category.setSort(0);
        categoryMapper.insert(category);
        return category.getId();
    }

    @Override
    public void update(Long id, String name, String code, Long tenantId) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        category.setName(name);
        category.setCode(code);
        categoryMapper.updateById(category);
    }

    @Override
    public void delete(Long id, Long tenantId) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        List<Category> children = categoryMapper.selectByParentId(id, tenantId);
        if (!children.isEmpty()) {
            throw new BusinessException(2003, "该分类下有子分类，无法删除");
        }
        categoryMapper.deleteById(id);
    }

    private CategoryVO buildTree(Category category, Long tenantId) {
        CategoryVO vo = toVO(category);
        List<Category> children = categoryMapper.selectByParentId(category.getId(), tenantId);
        vo.setChildren(children.stream()
                .map(child -> buildTree(child, tenantId))
                .collect(Collectors.toList()));
        return vo;
    }

    private CategoryVO toVO(Category category) {
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(category, vo);
        return vo;
    }
}

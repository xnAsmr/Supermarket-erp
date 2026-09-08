package com.supermarket.erp.module.system.service.impl;

import com.supermarket.erp.common.exception.BusinessException;
import com.supermarket.erp.module.system.entity.Menu;
import com.supermarket.erp.module.system.mapper.MenuMapper;
import com.supermarket.erp.module.system.service.IMenuService;
import com.supermarket.erp.module.system.vo.MenuVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements IMenuService {

    private final MenuMapper menuMapper;

    @Override
    public List<MenuVO> getTree() {
        List<Menu> roots = menuMapper.selectRootMenus();
        return roots.stream().map(this::buildTree).collect(Collectors.toList());
    }

    @Override
    public List<MenuVO> getChildren(Long parentId) {
        List<Menu> children = menuMapper.selectByParentId(parentId);
        return children.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public Long create(Menu menu) {
        if (menu.getParentId() == null) {
            menu.setParentId(0L);
        }
        if (menu.getSort() == null) {
            menu.setSort(0);
        }
        if (menu.getStatus() == null) {
            menu.setStatus(1);
        }
        if (menu.getVisible() == null) {
            menu.setVisible(1);
        }
        menuMapper.insert(menu);
        return menu.getId();
    }

    @Override
    public void update(Long id, Menu menu) {
        Menu existing = menuMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("菜单不存在");
        }
        menu.setId(id);
        menuMapper.updateById(menu);
    }

    @Override
    public void delete(Long id) {
        Menu existing = menuMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("菜单不存在");
        }
        List<Menu> children = menuMapper.selectByParentId(id);
        if (!children.isEmpty()) {
            throw new BusinessException(2003, "该分类下有子分类");
        }
        menuMapper.deleteById(id);
    }

    private MenuVO buildTree(Menu menu) {
        MenuVO vo = toVO(menu);
        List<Menu> children = menuMapper.selectByParentId(menu.getId());
        vo.setChildren(children.stream()
                .map(this::buildTree)
                .collect(Collectors.toList()));
        return vo;
    }

    @Override
    public List<MenuVO> getUserMenuTree(Long userId) {
        List<Menu> allMenus = menuMapper.selectMenusByUserId(userId);
        Map<Long, List<Menu>> byParent = allMenus.stream()
                .collect(Collectors.groupingBy(Menu::getParentId));
        return buildUserTree(byParent, 0L);
    }

    private List<MenuVO> buildUserTree(Map<Long, List<Menu>> byParent, Long parentId) {
        List<Menu> children = byParent.get(parentId);
        if (children == null) {
            return List.of();
        }
        return children.stream().map(menu -> {
            MenuVO vo = toVO(menu);
            vo.setChildren(buildUserTree(byParent, menu.getId()));
            return vo;
        }).collect(Collectors.toList());
    }

    private MenuVO toVO(Menu menu) {
        MenuVO vo = new MenuVO();
        BeanUtils.copyProperties(menu, vo);
        return vo;
    }
}

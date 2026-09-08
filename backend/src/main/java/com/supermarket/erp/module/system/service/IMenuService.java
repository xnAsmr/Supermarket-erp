package com.supermarket.erp.module.system.service;

import com.supermarket.erp.module.system.entity.Menu;
import com.supermarket.erp.module.system.vo.MenuVO;

import java.util.List;

public interface IMenuService {

    List<MenuVO> getTree();

    List<MenuVO> getChildren(Long parentId);

    Long create(Menu menu);

    void update(Long id, Menu menu);

    void delete(Long id);

    List<MenuVO> getUserMenuTree(Long userId);
}

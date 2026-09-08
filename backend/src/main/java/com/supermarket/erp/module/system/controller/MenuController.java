package com.supermarket.erp.module.system.controller;

import com.supermarket.erp.common.annotation.LogOperation;
import com.supermarket.erp.common.result.Result;
import com.supermarket.erp.common.util.SecurityUtil;
import com.supermarket.erp.module.system.entity.Menu;
import com.supermarket.erp.module.system.service.IMenuService;
import com.supermarket.erp.module.system.vo.MenuVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "菜单管理")
@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
public class MenuController {

    private final IMenuService menuService;

    private static final Integer SYSTEM_ADMIN_TYPE = 3;
    private static final Long TENANT_MENU_ID = 601L;

    @Operation(summary = "菜单树")
    @GetMapping("/tree")
    public Result<List<MenuVO>> tree() {
        return Result.success(menuService.getTree());
    }

    @Operation(summary = "当前用户菜单树")
    @GetMapping("/current")
    public Result<List<MenuVO>> currentMenus() {
        Long userId = SecurityUtil.getUserId();
        Integer userType = SecurityUtil.getLoginUser().getUserType();
        List<MenuVO> tree = menuService.getUserMenuTree(userId);
        if (!SYSTEM_ADMIN_TYPE.equals(userType)) {
            tree.removeIf(menu -> TENANT_MENU_ID.equals(menu.getId()));
        }
        return Result.success(tree);
    }

    @Operation(summary = "子菜单列表")
    @GetMapping("/children")
    public Result<List<MenuVO>> children(@RequestParam Long parentId) {
        return Result.success(menuService.getChildren(parentId));
    }

    @Operation(summary = "新增菜单")
    @LogOperation(module = "菜单管理", operation = "新增菜单")
    @PostMapping
    public Result<Long> create(@RequestBody Menu menu) {
        return Result.success(menuService.create(menu));
    }

    @Operation(summary = "修改菜单")
    @LogOperation(module = "菜单管理", operation = "修改菜单")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Menu menu) {
        menuService.update(id, menu);
        return Result.success();
    }

    @Operation(summary = "删除菜单")
    @LogOperation(module = "菜单管理", operation = "删除菜单")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return Result.success();
    }
}

package com.supermarket.erp.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.erp.common.exception.BusinessException;
import com.supermarket.erp.common.result.PageResult;
import com.supermarket.erp.module.system.dto.RoleCreateDTO;
import com.supermarket.erp.module.system.entity.Role;
import com.supermarket.erp.module.system.entity.RoleMenu;
import com.supermarket.erp.module.system.mapper.RoleMapper;
import com.supermarket.erp.module.system.mapper.RoleMenuMapper;
import com.supermarket.erp.module.system.service.IRoleService;
import com.supermarket.erp.module.system.vo.RoleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements IRoleService {

    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;

    @Override
    public PageResult<RoleVO> pageList(Long tenantId, Integer page, Integer pageSize) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getDeleted, 0);
        if (tenantId != null) {
            wrapper.eq(Role::getTenantId, tenantId);
        }
        wrapper.orderByDesc(Role::getCreateTime);

        Page<Role> pageParam = new Page<>(page, pageSize);
        Page<Role> result = roleMapper.selectPage(pageParam, wrapper);

        List<RoleVO> voList = result.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    @Override
    public RoleVO getById(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(404, "角色不存在");
        }
        RoleVO vo = toVO(role);
        List<RoleMenu> roleMenus = roleMenuMapper.selectByRoleId(id);
        vo.setMenuIds(roleMenus.stream().map(RoleMenu::getMenuId).collect(Collectors.toList()));
        return vo;
    }

    @Override
    @Transactional
    public Long create(RoleCreateDTO dto, Long tenantId) {
        LambdaQueryWrapper<Role> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(Role::getDeleted, 0)
                   .eq(Role::getTenantId, tenantId)
                   .eq(Role::getRoleCode, dto.getRoleCode());
        if (roleMapper.selectCount(checkWrapper) > 0) {
            throw new BusinessException(400, "该租户下角色编码已存在");
        }

        Role role = new Role();
        BeanUtils.copyProperties(dto, role);
        role.setTenantId(tenantId);
        role.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        roleMapper.insert(role);

        if (dto.getMenuIds() != null && !dto.getMenuIds().isEmpty()) {
            for (Long menuId : dto.getMenuIds()) {
                RoleMenu roleMenu = new RoleMenu();
                roleMenu.setRoleId(role.getId());
                roleMenu.setMenuId(menuId);
                roleMenuMapper.insert(roleMenu);
            }
        }

        return role.getId();
    }

    @Override
    @Transactional
    public void update(Long id, RoleCreateDTO dto, Long tenantId) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(404, "角色不存在");
        }

        LambdaQueryWrapper<Role> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(Role::getDeleted, 0)
                   .eq(Role::getTenantId, tenantId)
                   .eq(Role::getRoleCode, dto.getRoleCode())
                   .ne(Role::getId, id);
        if (roleMapper.selectCount(checkWrapper) > 0) {
            throw new BusinessException(400, "该租户下角色编码已存在");
        }

        BeanUtils.copyProperties(dto, role);
        roleMapper.updateById(role);

        roleMenuMapper.deleteByRoleId(id);

        if (dto.getMenuIds() != null && !dto.getMenuIds().isEmpty()) {
            for (Long menuId : dto.getMenuIds()) {
                RoleMenu roleMenu = new RoleMenu();
                roleMenu.setRoleId(id);
                roleMenu.setMenuId(menuId);
                roleMenuMapper.insert(roleMenu);
            }
        }
    }

    @Override
    public void delete(Long id, Long tenantId) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(404, "角色不存在");
        }
        roleMapper.deleteById(id);
    }

    private RoleVO toVO(Role role) {
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(role, vo);
        return vo;
    }
}
